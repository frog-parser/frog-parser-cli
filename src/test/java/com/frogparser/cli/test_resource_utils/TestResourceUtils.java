package com.frogparser.cli.test_resource_utils;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class TestResourceUtils {

    public static String readResourceAsString(String... pathSegment) {

        try {

            var file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX
                    .concat(String.join("/", pathSegment)));

            try (var fileInputStream = new FileInputStream(file);
                 var bufferedInputStream = new BufferedInputStream(fileInputStream)) {

                return StreamUtils.copyToString(bufferedInputStream, StandardCharsets.UTF_8);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
