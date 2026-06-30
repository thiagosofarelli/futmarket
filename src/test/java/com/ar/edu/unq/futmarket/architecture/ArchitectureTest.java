package com.ar.edu.unq.futmarket.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "com.ar.edu.unq.futmarket",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    private static final String BASE = "com.ar.edu.unq.futmarket";

    // --- Layer dependency rules ---

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_directly = noClasses()
            .that().resideInAPackage(BASE + ".controllers..")
            .should().dependOnClassesThat()
            .resideInAPackage(BASE + ".repositories..")
            .as("Controllers must not access repositories directly");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers = noClasses()
            .that().resideInAPackage(BASE + ".services..")
            .should().dependOnClassesThat()
            .resideInAPackage(BASE + ".controllers..")
            .as("Services must not depend on controllers");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_services_or_controllers = noClasses()
            .that().resideInAPackage(BASE + ".repositories..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    BASE + ".services..",
                    BASE + ".controllers.."
            )
            .as("Repositories must not depend on services or controllers");

    // --- Naming convention rules ---

    @ArchTest
    static final ArchRule rest_controllers_should_end_with_Controller = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .as("@RestController classes must end with 'Controller'");

    @ArchTest
    static final ArchRule service_implementations_should_end_with_ServiceImpl = classes()
            .that().resideInAPackage(BASE + ".services.impl")
            .and().areNotAnonymousClasses()
            .should().haveSimpleNameEndingWith("ServiceImpl")
            .as("Service implementations must end with 'ServiceImpl'");

    @ArchTest
    static final ArchRule interfaces_in_repositories_should_end_with_Repository = classes()
            .that().resideInAPackage(BASE + ".repositories")
            .should().haveSimpleNameEndingWith("Repository")
            .as("Classes in the repositories package must end with 'Repository'");

    // --- Annotation placement rules ---

    @ArchTest
    static final ArchRule rest_controllers_must_reside_in_controllers_package = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage(BASE + ".controllers..")
            .as("@RestController classes must reside in the controllers package");

    @ArchTest
    static final ArchRule service_beans_must_reside_in_services_package = classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAPackage(BASE + ".services..")
            .as("@Service classes must reside in the services package");

    // --- DTO confinement rule ---

    @ArchTest
    static final ArchRule dtos_must_not_leak_into_services = noClasses()
            .that().resideInAPackage(BASE + ".services..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    BASE + ".controllers.request..",
                    BASE + ".controllers.response..",
                    BASE + ".controllers.dto.."
            )
            .as("Services must not depend on controller DTOs (request/response/dto)");

    // --- No cyclic dependencies ---

    @ArchTest
    static final ArchRule no_cycles_between_top_level_packages = slices()
            .matching(BASE + ".(*)..")
            .should().beFreeOfCycles()
            .as("Top-level packages must not have cyclic dependencies");
}
