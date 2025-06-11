package com.example.codecheckerbackend.service;

import com.example.codecheckerbackend.model.CodeError;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CucumberTestService {

    public static class TestResult {
        private boolean success;
        private List<CodeError> errors;

        public TestResult(boolean success, List<CodeError> errors) {
            this.success = success;
            this.errors = errors;
        }

        public boolean isSuccess() {
            return success;
        }

        public List<CodeError> getErrors() {
            return errors;
        }
    }

    public TestResult runTests(String code, String className, String methodName, String testScenario) {
        List<CodeError> errors = new ArrayList<>();

        try {
            // Компилируем код
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            if (compiler == null) {
                errors.add(new CodeError(0, 0, "Компилятор Java не найден", "SEMANTIC"));
                return new TestResult(false, errors);
            }

            // Создаем временную директорию для классов
            Path tempDir = Files.createTempDirectory("test");

            // Компилируем код
            StringWriter writer = new StringWriter();
            JavaFileObject file = new InMemoryJavaFileObject(className, code);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    writer, null, null,
                    Arrays.asList("-d", tempDir.toString()),
                    null,
                    Arrays.asList(file)
            );

            boolean success = task.call();

            if (!success) {
                errors.add(new CodeError(0, 0, "Ошибка компиляции: " + writer.toString(), "SEMANTIC"));
                return new TestResult(false, errors);
            }

            // Загружаем скомпилированный класс
            URLClassLoader classLoader = URLClassLoader.newInstance(
                    new URL[] { tempDir.toUri().toURL() },
                    getClass().getClassLoader()
            );

            Class<?> clazz = Class.forName(className, true, classLoader);

            // Выполняем простые тесты на основе сценария
            TestResult result = executeScenarioTests(clazz, methodName, testScenario);

            // Удаляем временные файлы
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            // Ignore
                        }
                    });

            return result;

        } catch (Exception e) {
            errors.add(new CodeError(0, 0, "Ошибка выполнения теста: " + e.getMessage(), "SEMANTIC"));
            return new TestResult(false, errors);
        }
    }

    private TestResult executeScenarioTests(Class<?> clazz, String methodName, String scenario) {
        List<CodeError> errors = new ArrayList<>();

        try {
            // Парсим сценарий и выполняем тесты
            String[] lines = scenario.split("\n");

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("Given") || line.startsWith("When") || line.startsWith("Then")) {
                    // Простой парсер для базовых сценариев
                    if (line.contains("array") && line.contains("[")) {
                        // Извлекаем массив из сценария
                        String arrayStr = line.substring(line.indexOf("["), line.indexOf("]") + 1);
                        int[] array = parseArray(arrayStr);

                        // Создаем экземпляр класса и вызываем метод
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        Method method = clazz.getMethod(methodName, int[].class);

                        if (line.contains("sorted")) {
                            // Проверяем результат сортировки
                            method.invoke(instance, array);

                            if (!isSorted(array)) {
                                errors.add(new CodeError(0, 0,
                                        "Массив не отсортирован после вызова метода", "SEMANTIC"));
                            }
                        } else if (line.contains("max")) {
                            // Проверяем поиск максимума
                            int result = (int) method.invoke(instance, array);
                            int expected = getMax(array);

                            if (result != expected) {
                                errors.add(new CodeError(0, 0,
                                        String.format("Ожидался максимум %d, получен %d", expected, result),
                                        "SEMANTIC"));
                            }
                        }
                    }
                }
            }

            return new TestResult(errors.isEmpty(), errors);

        } catch (Exception e) {
            errors.add(new CodeError(0, 0,
                    "Ошибка при выполнении сценария: " + e.getMessage(), "SEMANTIC"));
            return new TestResult(false, errors);
        }
    }

    private int[] parseArray(String arrayStr) {
        arrayStr = arrayStr.replaceAll("[\\[\\]]", "");
        String[] parts = arrayStr.split(",");
        int[] array = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            array[i] = Integer.parseInt(parts[i].trim());
        }

        return array;
    }

    private boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i-1]) {
                return false;
            }
        }
        return true;
    }

    private int getMax(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    // Вспомогательный класс для компиляции кода в памяти
    static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        public InMemoryJavaFileObject(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}