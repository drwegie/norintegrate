terraform {
  backend "s3" {
    bucket         = "norintegrate-terraform-state"
    key            = "infra/terraform.tfstate"
    region         = "eu-north-1"
    dynamodb_table = "norintegrate-terraform-locks"
    encrypt        = true
  }
}
