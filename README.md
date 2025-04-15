## Custom JWT Token Issuer (IS 7.1.0)

This project includes an implementation extending the default JWT access token issuer `JWTTokenIssuer`
to override the `createJWTClaimSet` method and include additional claims to the JWT claim set pulled from the
URL parameters in HTTP request's query string.

**Important:** Since the custom claim values doesn't actually exist in the user store, they won't show up in the ID token or the userinfo response. 

---

### Implementation Steps:

1. Create a maven project with a custom class extending the `JWTTokenIssuer` class, and override the `createJWTClaimSet` method.
2. Add the necessary dependencies in the `pom.xml` (e.g., `org.wso2.carbon.identity.inbound.auth.oauth2:org.wso2.carbon.identity.oauth`).
   - The correct dependency versions can be found in the WSO2 Identity Server's `pom.xml` file in the `product-is` [repository](https://github.com/wso2/product-is/blob/v7.1.0/pom.xml#L2457).
3. Include the logic to pull data from the HTTP request wrapper and add it to the JWT claims on a new `JWTClaimsSet.Builder` instance.
4. Build the Maven project (e.g., `mvn clean install`).

---

### Configuration:
1. Copy the JAR file from the `<PROJECT_HOME>/target` folder to the `<IS_HOME>/repository/components/lib` directory.
2. Add the configuration below in the `<IS_HOME>/repository/conf/deployment.toml` file:

    ```toml
    [oauth.extensions.token_types.token_type]
    name = "Custom_JWT"
    issuer = "org.sample.token.issuer.CustomJWTAccessTokenIssuer"
    persist_access_token_alias = false
    ```
    * _Note on `name`: Its value will be shown in the Service Provider's under 'Token Types'._
    * _Note on `issuer`: Replace `org.sample.token.issuer.CustomJWTAccessTokenIssuer` with your custom class name._
    * _Note on `persist_access_token_alias`: This property is set to `false` to avoid persisting the actual access token value as the token alias in the cache and database. 
   JWT access tokens have a `jti` field for JWT access tokens which is the value that should get persisted instead._

3. Start the WSO2 Identity Server.
4. [Create a custom attribute](https://is.docs.wso2.com/en/7.1.0/guides/users/attributes/manage-attributes/#add-custom-attributes) for each of the claims you want to add to the JWT claim set. 
For example, if you want to add `custom_claim_1` and `custom_claim_2`, create the attribute with the same name.
5. Include the new [OIDC claims](https://is.docs.wso2.com/en/7.1.0/guides/users/attributes/manage-oidc-attribute-mappings/) to an existing or new [OIDC scope](https://is.docs.wso2.com/en/7.1.0/guides/users/attributes/manage-scopes/) (the application must include the scope in the authorisation/token flow).
6. [Create a new Service Provider or edit an existing one](https://is.docs.wso2.com/en/7.1.0/guides/applications/), and [include the OIDC scope in the Service Provider's OIDC scopes](https://is.docs.wso2.com/en/7.1.0/guides/authentication/user-attributes/enable-attributes-for-oidc-app/#select-user-attributes).

---

### Logging:

For this component's logs to be printed, you need to do the following steps in to the `<IS_HOME>/repository/conf/log4j2.properties` file:

1. Create a [Log4J2 Logger](https://logging.apache.org/log4j/2.x/manual/configuration.html#configuring-loggers) named `org-sample` mapped to the `org.sample` package:
   ```properties
   logger.org-sample.name = org.sample
   logger.org-sample.level = DEBUG
   ```
2. Add the new `org-sample` logger to the `loggers` variable:
   ```properties
   loggers = AUDIT_LOG, . . ., org-sample
   ```

#### Example output:
```
. . . WARN {org.sample.token.issuer.CustomJWTAccessTokenIssuer} - Both client channel and client version are null or empty. Not adding to JWT claim set.
. . . WARN {org.sample.token.issuer.CustomJWTAccessTokenIssuer} - Client channel is null or empty. Not adding to JWT claim set.
. . . WARN {org.sample.token.issuer.CustomJWTAccessTokenIssuer} - Client version is null or empty. Not adding to JWT claim set.
```
