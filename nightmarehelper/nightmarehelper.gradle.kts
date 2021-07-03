version = "1.0.9"

project.extra["PluginName"] = "Nightmare Auto Prayerv2"
project.extra["PluginDescription"] = "Automatically swap prayers in Nightmare of Ashihama"

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
