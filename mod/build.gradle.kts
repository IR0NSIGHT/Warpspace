plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))

    }
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
tasks.register("world") {
    dependsOn("hello")
    doLast {
        println(" world!")
    }
}

version = "0.18.0"

