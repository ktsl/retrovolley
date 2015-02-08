/*
 * Copyright (C) 2015 Serghei (Serj) Lotutovici
 * Copyright (C) 2015 Konstantin Tarasenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrovolley.request;

import retrovolley.EndpointAdapter;
import retrovolley.RetroVolley;
import retrovolley.annotation.*;
import retrovolley.rest.RestCall;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A request info wrapper that is created form a RestCall implementation
 *
 * @author Serghei Lotutovici
 */
class RequestInfo {

    /**
     * Upper and lower characters, digits, underscores, and hyphens, starting with a character
     */
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";

    /**
     * Parameter name pattern
     */
    private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);

    /**
     * Parameter value validator
     */
    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

    private int mMethod;
    private String mPath;
    private Set<String> mRestParams;
    private EndpointAdapter mEndpointAdapter;
    private java.lang.reflect.Type mResponseType;
    private boolean mHateoas = false;
    private boolean mDynamic = false;
    private int mMaxNumRetries = -1;

    /**
     * Build a rest info object
     *
     * @param restCall The rest call object
     */
    RequestInfo(RestCall restCall) {
        /* Throw exception on if rest call is null */
        validateRestCallObject(restCall);

        /* Initialize fields */
        Annotation[] annotations = extractAnnotations(restCall);
        extractFieldValues(annotations);

        /* If response type not initialized then it equals string */
        if (mResponseType == null) {
            mResponseType = String.class;
        }
    }

    /**
     * Extract filed values from restCall annotations
     * <p/>
     * TODO add more validations and type checking
     *
     * @param annotations The provided objects annotations
     */
    @SuppressWarnings("unchecked")
    private void extractFieldValues(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            RestMethod restMethod = null;

            /* Check if the the restCall contains a hateoas annotation */
            if (annotationType == Hateoas.class) {
                mHateoas = true;
            }

            /* Check if the restCall contains a dynamic annotation */
            if (annotationType == Dynamic.class) {
                mDynamic = true;

                /* Parse method value from dynamic annotation */
                try {
                    mMethod = ((Dynamic) annotation).value().method;
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format(
                            "Failed to extract method value from @%s annotation.",
                            annotationType)
                    );
                }
            }

            /* Check if we need to get a rest method object */
            if (!mHateoas && !mDynamic) {
                /* Parse inner annotations to get the RestMethod values */
                for (Annotation innerAnnotation : annotationType.getAnnotations()) {
                    if (RestMethod.class == innerAnnotation.annotationType()) {
                        /* Get rest method annotation */
                        restMethod = (RestMethod) innerAnnotation;

                        /* Exit loop and proceed */
                        break;
                    }
                }
            }

            /* Parse annotations based on restMethod result */
            if (restMethod != null) {
                /* If mPath is set then we already have parsed a similar annotation */
                if (mPath != null) {
                    throw new IllegalArgumentException("Only one RestMethod annotation allowed: " + this.toString());
                }

                /* Parse method value */
                try {
                    mMethod = restMethod.method().method;
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format(
                            "Failed to extract method value from @%s annotation.",
                            restMethod.annotationType())
                    );
                }

                /* Parse rest method path */
                String path;
                try {
                    path = (String) annotationType.getMethod("value").invoke(annotation);
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format(
                            "Failed to extract String 'value' from @%s annotation.",
                            annotationType.getSimpleName())
                    );
                }

                /* Parse the path and extract additional parameters */
                parsePath(path);

            } else if (annotationType == Response.class) {
                mResponseType = ((Response) annotation).value();

            } else if (annotationType == Endpoint.class) {
                /* Get endpoint name */
                String endpointName = ((Endpoint) annotation).value();
                /* Check cache instance if instance already created */
                EndpointAdapter adapter = RetroVolley.getInstance().getAdapter(endpointName);
                /* Get local reference to endpoint */
                if (adapter == null) {
                    throw new IllegalStateException("Could not get adapter for name: " + endpointName);
                }
                mEndpointAdapter = adapter;
            } else if (annotationType == MaxRetryNumber.class) {
                mMaxNumRetries = ((MaxRetryNumber) annotation).value();

            }
        }

        // TODO Post parse actions. Init with defaults
    }

    /**
     * Parse and validate the request path. Extract rest parameters from the path.
     *
     * @param path The rest path to parse
     */
    private void parsePath(String path) {
        /* Throw exception if the path is not parcelable */
        if (path == null || path.length() == 0 || path.charAt(0) != '/') {
            throw new IllegalArgumentException("The path must not be null, ether empty and start with '/'");
        }

        /* Parse set of parameters */
        Set<String> restParams = parsePathParameters(path);

        /* Save path and parameters */
        mPath = path;
        mRestParams = restParams;
    }

    public int getMethod() {
        return mMethod;
    }

    protected void setMethod(int method) {
        mMethod = method;
    }

    /**
     * Get the request url
     *
     * @return A valid url for the request
     */
    public String getUrl() {
        return mEndpointAdapter.getEndpoint() + mPath;
    }

    /**
     * @return A set of available rest parameters
     */
    public Set<String> getRestParams() {
        return mRestParams;
    }

    public EndpointAdapter getEndpoint() {
        return mEndpointAdapter;
    }

    public java.lang.reflect.Type getResponseType() {
        return mResponseType;
    }

    public int getMaxNumRetries() {
        return mMaxNumRetries;
    }

    protected boolean isHateoas() {
        return mHateoas;
    }

    protected boolean isDynamic() {
        return mDynamic;
    }


    /**
     * Simple restCall validation
     *
     * @param restCall The restCall object to validate
     * @param <V>      Generic type
     */
    private static <V> void validateRestCallObject(V restCall) {
        if (restCall == null) {
            throw new NullPointerException("Rest Call must not be null");
        }

        if (restCall.getClass().isInterface()) {
            throw new IllegalArgumentException("The restCall object must not be an interface");
        }

        if (!restCall.getClass().isEnum()) {
            throw new IllegalArgumentException("The restCall object must be an enum");
        }
    }

    /**
     * Parse the restCall object annotations
     *
     * @param restCall The restCall
     * @return An array of all annotations
     */
    private static Annotation[] extractAnnotations(RestCall restCall) {
        try {
            /* All enums toString methods return there names */
            Field field = restCall.getClass().getField(restCall.toString());
            /* Return declared annotations */
            return field.getDeclaredAnnotations();
        } catch (NoSuchFieldException e) {
            /* Throw exception, if a non enum vas passed */
            throw new UnsupportedOperationException("Can't parse annotations");
        }
    }

    /**
     * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
     * in the URI, it will only show up once in the set.
     */
    private static Set<String> parsePathParameters(String path) {
        Matcher matcher = PARAM_URL_REGEX.matcher(path);
        Set<String> patterns = new LinkedHashSet<String>();

        while (matcher.find()) {
            patterns.add(matcher.group(1));
        }

        return patterns;
    }

    /**
     * Utility method to validate the parameter name
     *
     * @param requestInfo The RequestInfo object that should contain the path with the specific param
     * @param name        The name of the parameter
     * @throws java.lang.IllegalArgumentException If name and acceptance criteria ar not met.
     */
    static void validateParameterName(RequestInfo requestInfo, String name) {
        /* Verify that the name matches the pattern */
        if (!PARAM_NAME_REGEX.matcher(name).matches()) {
            throw new IllegalArgumentException(String.format(
                    "URL REST parameter must match pattern: %s. Found: %s",
                    PARAM_URL_REGEX.pattern(),
                    name
            ));
        }

        /* Verify URL replacement name is actually present in the URL path */
        final boolean urlHasParams = requestInfo != null && requestInfo.getRestParams() != null;
        if (!urlHasParams || !requestInfo.getRestParams().contains(name)) {
            throw new IllegalArgumentException(String.format(
                    "URL \"%s\" does not contain \"{%s}\".",
                    requestInfo != null ? requestInfo.mPath : null,
                    name
            ));
        }
    }

}