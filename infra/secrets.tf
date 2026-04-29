resource "aws_secretsmanager_secret" "ghcr_credentials" {
  name        = "${local.name_prefix}/ghcr-credentials"
  description = "GHCR pull credentials for ECS (set value manually after creation)"
}

resource "aws_secretsmanager_secret" "jwt_issuer_uri" {
  name        = "${local.name_prefix}/jwt-issuer-uri"
  description = "OAuth JWT issuer URI for the API"
}

resource "aws_secretsmanager_secret" "cors_origins" {
  name        = "${local.name_prefix}/cors-allowed-origins"
  description = "Comma-separated CORS allowed origins for the API"
}

resource "aws_secretsmanager_secret" "nextauth_secret" {
  name        = "${local.name_prefix}/nextauth-secret"
  description = "NextAuth.js session encryption secret"
}

resource "aws_secretsmanager_secret" "google_oauth" {
  name        = "${local.name_prefix}/google-oauth"
  description = "Google OAuth client ID and secret (JSON: {client_id, client_secret})"
}
