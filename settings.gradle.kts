// Settings for the OENEXA AI Core multi-module project
rootProject.name = "oenexa-ai-core"

// Foundation Hexagonal Core
include(
    "domain",
    "application",
    "infrastructure",
    "api"
)
