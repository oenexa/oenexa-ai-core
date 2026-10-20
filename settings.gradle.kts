// Settings for the OENEXA AI Engineer Backend multi-module project
rootProject.name = "oenexa-ai-engineer-backend"

// Foundation Hexagonal Core
include(
    "domain",
    "application",
    "infrastructure",
    "api"
)
