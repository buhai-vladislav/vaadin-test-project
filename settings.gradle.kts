pluginManagement {
    repositories {
        maven("https://maven.vaadin.com/vaadin-prereleases")
        gradlePluginPortal()
    }
    plugins {
        id("com.vaadin") version (providers.gradleProperty("vaadinVersion").orElse("24.5.1").get())
    }
}
