plugins {
    `java-library`
}

java {

    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_7
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_7
    withJavadocJar()

}

tasks.register("hello") {
    doLast {
        val someString = "owo_bOI5000"
        println("var: $someString")
        println("last and least")
        repeat (4) {println("i hate blindflug")}
        println("hello ")
    }

}
tasks.register("hello-world") {
    dependsOn("hello")
    doLast {
        println(" world!")
    }
}

version = "0.18.0"

