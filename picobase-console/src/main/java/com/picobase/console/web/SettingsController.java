package com.picobase.console.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    @GetMapping
    public String list() {
        return """
                {
                    "meta": {
                        "appName": "Acme",
                        "appUrl": "https://test.pocketbase.io/",
                        "hideControls": false,
                        "senderName": "Support",
                        "senderAddress": "support@example.com",
                        "verificationTemplate": {
                            "body": "\\u003cp\\u003eHello,\\u003c/p\\u003e\\n\\u003cp\\u003eThank you for joining us at {APP_NAME}.\\u003c/p\\u003e\\n\\u003cp\\u003eClick on the button below to verify your email address.\\u003c/p\\u003e\\n\\u003cp\\u003e\\n  \\u003ca class=\\"btn\\" href=\\"{ACTION_URL}\\" target=\\"_blank\\" rel=\\"noopener\\"\\u003eVerify\\u003c/a\\u003e\\n\\u003c/p\\u003e\\n\\u003cp\\u003e\\n  Thanks,\\u003cbr/\\u003e\\n  {APP_NAME} team\\n\\u003c/p\\u003e",
                            "subject": "Verify your {APP_NAME} email",
                            "actionUrl": "{APP_URL}/_/#/auth/confirm-verification/{TOKEN}",
                            "hidden": false
                        },
                        "resetPasswordTemplate": {
                            "body": "\\u003cp\\u003eHello,\\u003c/p\\u003e\\n\\u003cp\\u003eClick on the button below to reset your password.\\u003c/p\\u003e\\n\\u003cp\\u003e\\n  \\u003ca class=\\"btn\\" href=\\"{ACTION_URL}\\" target=\\"_blank\\" rel=\\"noopener\\"\\u003eReset password\\u003c/a\\u003e\\n\\u003c/p\\u003e\\n\\u003cp\\u003e\\u003ci\\u003eIf you didn't ask to reset your password, you can ignore this email.\\u003c/i\\u003e\\u003c/p\\u003e\\n\\u003cp\\u003e\\n  Thanks,\\u003cbr/\\u003e\\n  {APP_NAME} team\\n\\u003c/p\\u003e",
                            "subject": "Reset your {APP_NAME} password",
                            "actionUrl": "{APP_URL}/_/#/auth/confirm-password-reset/{TOKEN}",
                            "hidden": false
                        },
                        "confirmEmailChangeTemplate": {
                            "body": "\\u003cp\\u003eHello,\\u003c/p\\u003e\\n\\u003cp\\u003eClick on the button below to confirm your new email address.\\u003c/p\\u003e\\n\\u003cp\\u003e\\n  \\u003ca class=\\"btn\\" href=\\"{ACTION_URL}\\" target=\\"_blank\\" rel=\\"noopener\\"\\u003eConfirm new email\\u003c/a\\u003e\\n\\u003c/p\\u003e\\n\\u003cp\\u003e\\u003ci\\u003eIf you didn't ask to change your email address, you can ignore this email.\\u003c/i\\u003e\\u003c/p\\u003e\\n\\u003cp\\u003e\\n  Thanks,\\u003cbr/\\u003e\\n  {APP_NAME} team\\n\\u003c/p\\u003e",
                            "subject": "Confirm your {APP_NAME} new email address",
                            "actionUrl": "{APP_URL}/_/#/auth/confirm-email-change/{TOKEN}",
                            "hidden": false
                        }
                    },
                    "logs": {
                        "maxDays": 7,
                        "minLevel": 0,
                        "logIp": true
                    },
                    "smtp": {
                        "enabled": false,
                        "host": "smtp.example.com",
                        "port": 587,
                        "username": "",
                        "password": "",
                        "authMethod": "",
                        "tls": true,
                        "localName": ""
                    },
                    "s3": {
                        "enabled": false,
                        "bucket": "",
                        "region": "",
                        "endpoint": "",
                        "accessKey": "",
                        "secret": "",
                        "forcePathStyle": false
                    },
                    "backups": {
                        "cron": "",
                        "cronMaxKeep": 3,
                        "s3": {
                            "enabled": false,
                            "bucket": "",
                            "region": "",
                            "endpoint": "",
                            "accessKey": "",
                            "secret": "",
                            "forcePathStyle": false
                        }
                    },
                    "adminAuthToken": {
                        "secret": "******",
                        "duration": 1209600
                    },
                    "adminPasswordResetToken": {
                        "secret": "******",
                        "duration": 1800
                    },
                    "adminFileToken": {
                        "secret": "******",
                        "duration": 120
                    },
                    "recordAuthToken": {
                        "secret": "******",
                        "duration": 1209600
                    },
                    "recordPasswordResetToken": {
                        "secret": "******",
                        "duration": 1800
                    },
                    "recordEmailChangeToken": {
                        "secret": "******",
                        "duration": 1800
                    },
                    "recordVerificationToken": {
                        "secret": "******",
                        "duration": 604800
                    },
                    "recordFileToken": {
                        "secret": "******",
                        "duration": 120
                    },
                    "emailAuth": {
                        "enabled": true,
                        "exceptDomains": null,
                        "onlyDomains": null,
                        "minPasswordLength": 8
                    },
                    "googleAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "facebookAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "githubAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "gitlabAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "discordAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "twitterAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "microsoftAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "spotifyAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "kakaoAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "twitchAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "stravaAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "giteeAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "livechatAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "giteaAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "oidcAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "oidc2Auth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "oidc3Auth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "appleAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "instagramAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "vkAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "yandexAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "patreonAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "mailcowAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    },
                    "bitbucketAuth": {
                        "enabled": false,
                        "clientId": "",
                        "clientSecret": "",
                        "authUrl": "",
                        "tokenUrl": "",
                        "userApiUrl": "",
                        "displayName": "",
                        "pkce": null
                    }
                }
                """;
    }
}
