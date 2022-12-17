package org.example;

import freemarker.template.Configuration;
import freemarker.template.Version;
import spark.Route;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;
import org.texttechnologylab.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    private static final Map<String, TemplateViewRoute> GUI_GET_ROUTES = new HashMap<>();
    private static final Map<String, Route> API_GET_ROUTES = new HashMap<>();
    private static final Configuration CONFIGURATION = new Configuration(Configuration.VERSION_2_3_26);

    public static void registerSparkRoutes() throws IOException {

        List<Class<? extends Handler>> classes = getHandlerClasses("gui");
        classes.addAll(getHandlerClasses("api"));

        classes.forEach(clazz -> {
            try {
                clazz.newInstance().register();
            } catch (Exception ignored) {};
        });

        String templatePath = resourceDirPath("templates");
        CONFIGURATION.setDirectoryForTemplateLoading(new File(templatePath));
        FreeMarkerEngine engine = new FreeMarkerEngine(CONFIGURATION);

        GUI_GET_ROUTES.forEach((path, route) -> {
            spark.Spark.get(path, route, engine);
        });
    }

    public static String resourceDirPath(String dirName) throws FileNotFoundException {
        URL resource = Main.class.getClassLoader().getResource(dirName);
        if (resource == null) throw new FileNotFoundException();
        try {
            return URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Standard Charset not found in java runtime environment");
        }
    }

    public static List<Class<? extends Handler>> getHandlerClasses(String subPackage) throws IOException {
        List<URL> resources = Collections.list(Thread.currentThread().getContextClassLoader().getResources(subPackage));
        List<Class<? extends Handler>> classes = new ArrayList<>();
        resources.forEach(resource -> {
            try {
                Class c = Class.forName(resource.getFile());
                if (Handler.class.isAssignableFrom(c)) {
                    classes.add((Class<? extends Handler>) c);
                }
            } catch (Exception e) {
                System.out.println("Cannot register handlers for class " +resource.getFile());
            }
        });
        return classes;
    }

    public static void registerGUIGetRoute(String path, TemplateViewRoute route) {
        GUI_GET_ROUTES.put(path, route);
    }

    public static void registerAPIGetRoute(String path, Route route) {
        API_GET_ROUTES.put(path, route);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        registerSparkRoutes();
    }
}