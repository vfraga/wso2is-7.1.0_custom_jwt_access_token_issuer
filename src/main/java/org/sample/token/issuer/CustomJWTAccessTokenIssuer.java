package org.sample.token.issuer;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.JWTTokenIssuer;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import javax.servlet.http.HttpServletRequestWrapper;

public class CustomJWTAccessTokenIssuer extends JWTTokenIssuer {
    private static final Log log = LogFactory.getLog(CustomJWTAccessTokenIssuer.class);

    public CustomJWTAccessTokenIssuer() throws IdentityOAuth2Exception {
        // constructor matching the super class
    }

    @Override
    protected JWTClaimsSet createJWTClaimSet(final OAuthAuthzReqMessageContext authAuthzReqMessageContext,
                                             final OAuthTokenReqMessageContext tokenReqMessageContext,
                                             final String consumerKey) throws IdentityOAuth2Exception {
        final JWTClaimsSet jwtClaimSet = super.createJWTClaimSet(authAuthzReqMessageContext, tokenReqMessageContext, consumerKey);

        try {
            final HttpServletRequestWrapper requestWrapper;

            if (tokenReqMessageContext != null) {
                // Extracting URL parameters in the query string from the request to the token endpoint
                // e.g., https://localhost:9443/oauth2/token?param1=value1&param2=value2 -> {param1=value1, param2=value2}
                requestWrapper = tokenReqMessageContext.getOauth2AccessTokenReqDTO().getHttpServletRequestWrapper();
            } else if (authAuthzReqMessageContext != null) {
                // Extracting URL parameters in the query string from the request to the authorization endpoint
                // e.g., https://localhost:9443/oauth2/authorize?param1=value1&param2=value2 -> {param1=value1, param2=value2}
                // Important: This code branch will most likely not get executed since there's no claims returned from the authorization endpoint.
                requestWrapper = authAuthzReqMessageContext.getAuthorizationReqDTO().getHttpServletRequestWrapper();
            } else {
                log.error("Unable to extract request wrapper. Returning default JWT claim set.");
                return jwtClaimSet;
            }

            final String clientChannel = requestWrapper.getParameter(Constants.CLIENT_CHANNEL);
            final String clientVersion = requestWrapper.getParameter(Constants.CLIENT_VERSION);

            // Evaluate if client channel and client version are present only once
            final boolean isClientChannelPresent = StringUtils.isNotBlank(clientChannel);
            final boolean isClientVersionPresent = StringUtils.isNotBlank(clientVersion);

            if (isClientChannelPresent || isClientVersionPresent) {
                // Only create a new JWTClaimsSet object if either client channel or client version is present
                final JWTClaimsSet.Builder jwtClaimSetBuilder = new JWTClaimsSet.Builder(jwtClaimSet);

                if (isClientChannelPresent) {
                    jwtClaimSetBuilder.claim(Constants.CLIENT_CHANNEL, clientChannel);
                } else {
                    log.warn("Client channel is null or empty. Not adding to JWT claim set.");
                }

                if (isClientVersionPresent) {
                    jwtClaimSetBuilder.claim(Constants.CLIENT_VERSION, clientVersion);
                } else {
                    log.warn("Client version is null or empty. Not adding to JWT claim set.");
                }

                return jwtClaimSetBuilder.build();
            } else {
                log.warn("Both client channel and client version are null or empty. Not adding to JWT claim set.");
                return jwtClaimSet;
            }
        } catch (Exception e) {
            log.error("Error adding custom claims to JWT claim set. Returning default values only.", e);
        }

        return jwtClaimSet;
    }

    private static final class Constants {
        private static final String CLIENT_CHANNEL = "clientChannel";
        private static final String CLIENT_VERSION = "clientVersion";
    }
}
