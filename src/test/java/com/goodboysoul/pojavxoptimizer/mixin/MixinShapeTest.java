package com.goodboysoul.pojavxoptimizer.mixin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixinShapeTest {

    private static final Path MIXIN_CLASSES = Paths.get(
            "build", "classes", "java", "main",
            "com", "goodboysoul", "pojavxoptimizer", "mixin", "client");

    private static final class Findings {
        final List<String> staticMethods = new ArrayList<>();
        final List<String> staticFields = new ArrayList<>();
        int classCount;
    }

    private static Findings scan() throws IOException {
        assertTrue(Files.isDirectory(MIXIN_CLASSES),
                "compiled mixin classes not found at " + MIXIN_CLASSES.toAbsolutePath()
                        + " - run the build before the tests");

        Findings findings = new Findings();
        try (Stream<Path> walk = Files.walk(MIXIN_CLASSES)) {
            List<Path> classFiles = walk
                    .filter(p -> p.toString().endsWith(".class"))
                    .filter(p -> !p.getFileName().toString().contains("$"))
                    .sorted()
                    .toList();

            assertFalse(classFiles.isEmpty(), "no mixin classes were compiled");

            for (Path classFile : classFiles) {
                try (InputStream in = Files.newInputStream(classFile)) {
                    ClassReader reader = new ClassReader(in);
                    reader.accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                         String signature, String[] exceptions) {
                            findings.classCount++;
                            if ((access & Opcodes.ACC_STATIC) != 0) {
                                findings.staticMethods.add(
                                        classFile.getFileName() + " -> " + name + descriptor);
                            }
                            return null;
                        }

                        @Override
                        public FieldVisitor visitField(int access, String name, String descriptor,
                                                       String signature, Object value) {
                            if ((access & Opcodes.ACC_STATIC) != 0) {
                                findings.staticFields.add(
                                        classFile.getFileName() + " -> " + name);
                            }
                            return null;
                        }
                    }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
                }
            }
        }
        return findings;
    }

    @Test
    @DisplayName("no mixin declares a static method, which Mixin rejects at startup")
    void noMixinDeclaresAStaticMethod() throws IOException {
        Findings findings = scan();

        assertTrue(findings.staticMethods.isEmpty(),
                "Mixin throws InvalidMixinException for non-private static methods in a mixin "
                        + "class, and that crashes the game before the main menu. Found: "
                        + findings.staticMethods);
    }

    @Test
    @DisplayName("no mixin declares a static field either")
    void noMixinDeclaresAStaticField() throws IOException {
        Findings findings = scan();

        assertTrue(findings.staticFields.isEmpty(),
                "Static state in a mixin merges into the target class and breaks when that class is "
                        + "instantiated more than once. Hold shared state in a plain class instead. "
                        + "Found: " + findings.staticFields);
    }

    @Test
    @DisplayName("the scan actually saw the mixin classes")
    void theScanIsNotEmpty() throws IOException {
        Findings findings = scan();
        assertTrue(findings.classCount >= 6,
                "expected at least six mixins to be scanned, saw " + findings.classCount);
    }
}
