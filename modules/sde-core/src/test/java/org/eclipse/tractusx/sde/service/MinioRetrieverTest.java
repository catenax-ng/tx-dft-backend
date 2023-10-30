package org.eclipse.tractusx.sde.service;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.sde.EnableTestContainers;
import org.eclipse.tractusx.sde.TestContainerInitializer;
import org.eclipse.tractusx.sde.core.csv.service.CsvHandlerService;
import org.eclipse.tractusx.sde.retrieverl.RetrieverI;
import org.eclipse.tractusx.sde.retrieverl.service.MinioRetrieverFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@SpringBootTest
@EnableTestContainers
@Execution(ExecutionMode.SAME_THREAD)
@ActiveProfiles("test")
@WithMockUser(username = "Admin", authorities = { "Admin" })
public class MinioRetrieverTest {
    @Autowired
    CsvHandlerService csvHandlerService;

    @Autowired
    MinioRetrieverFactoryImpl minioRetrieverFactory;


    @BeforeEach
    public void before() {
        TestContainerInitializer.minio.stop();
        TestContainerInitializer.minio.start();
    }


    @FunctionalInterface
    interface ThrowableExec {
        void exec(String param) throws IOException;
    }

    @RequiredArgsConstructor
    @ToString(of = "name")
    static class TestMethod implements Function<RetrieverI, MinioRetrieverTest.ThrowableExec> {
        @Delegate
        private final Function<RetrieverI, MinioRetrieverTest.ThrowableExec> delegate;
        private final String name;
    }

    static Stream<Function<RetrieverI, MinioRetrieverTest.ThrowableExec>> provider() {
        return Stream.of(new MinioRetrieverTest.TestMethod(r -> r::setSuccess, "SetSuccess"),
                new MinioRetrieverTest.TestMethod(r -> r::setFailed, "SetFailed"), new MinioRetrieverTest.TestMethod(r -> r::setPartial, "SetPartial"),
                new MinioRetrieverTest.TestMethod(r -> r::setProgress, "SetProgress"));
    }

    @ParameterizedTest(name = "testMinio Test: {index}, {argumentsWithNames}")
    @MethodSource("provider")
    void testFtps(Function<RetrieverI, MinioRetrieverTest.ThrowableExec> tr) throws Exception {
        try (var minio = minioRetrieverFactory.create()) {
            for (String fileId : minio) {
                final var filePath = Path.of(csvHandlerService.getFilePath(fileId));
                log.info(fileId);
                tr.apply(minio).exec(fileId);
                Files.copy(filePath, System.out);
                Files.delete(filePath);
            }
        }
    }
}
