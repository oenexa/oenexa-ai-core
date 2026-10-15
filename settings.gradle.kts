// Settings for the OENEXA AI Engineer multi‑module project
rootProject.name = "oenexa-ai-engineer"

// Foundation Hexagonal Core
include(
    "domain",
    "application",
    "infrastructure",
    "api"
)

// Enterprise Oenexa Platform Modules
val platformModules = listOf(
    "gateway", "auth", "core", "common", "chat", "code", "agent", "rag", "search",
    "security", "documentation", "review", "testing", "devops", "pipeline",
    "observability", "repository", "workspace", "admin"
)
platformModules.forEach { include("oenexa-$it") }
