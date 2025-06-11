package com.example.codecheckerbackend.service;

import com.example.codecheckerbackend.model.CodeError;
import com.example.codecheckerbackend.parser.JavaLexer;
import com.example.codecheckerbackend.parser.JavaParser;
import org.antlr.v4.runtime.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeAnalysisService {

    public static class AnalysisResult {
        private boolean syntaxValid;
        private List<CodeError> errors;

        public AnalysisResult(boolean syntaxValid, List<CodeError> errors) {
            this.syntaxValid = syntaxValid;
            this.errors = errors;
        }

        public boolean isSyntaxValid() {
            return syntaxValid;
        }

        public List<CodeError> getErrors() {
            return errors;
        }
    }

    public AnalysisResult analyzeCode(String code) {
        List<CodeError> errors = new ArrayList<>();

        try {
            // Создаем лексер
            JavaLexer lexer = new JavaLexer(CharStreams.fromString(code));
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Создаем парсер
            JavaParser parser = new JavaParser(tokens);

            // Добавляем обработчик ошибок
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    errors.add(new CodeError(line, charPositionInLine, msg, "SYNTAX"));
                }
            });

            // Парсим код
            parser.compilationUnit();

            return new AnalysisResult(errors.isEmpty(), errors);

        } catch (Exception e) {
            errors.add(new CodeError(0, 0, "Ошибка анализа: " + e.getMessage(), "SYNTAX"));
            return new AnalysisResult(false, errors);
        }
    }
}