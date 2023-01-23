import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "com.volodya262"
version = "1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2021.0.5"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

dependencies {
//    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    runtimeOnly("org.postgresql:postgresql")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.1")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.commons:commons-compress:1.22")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("com.jayway.jsonpath:json-path:2.7.0")
    testImplementation("com.jayway.jsonpath:json-path-assert:2.7.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
//        allWarningsAsErrors = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
