package com.kp.gen.spring;

import org.apache.commons.lang3.text.WordUtils;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// co the dung StringBuilder nhung met bo me di dc

public class GenSpringClass {


    public static void gen(String pakageEntities,String basePath ) throws Exception {

        Reflections reflections = new Reflections(pakageEntities);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
        List<MyEntity> myEntities = new LinkedList<>();
        for (Class<?> bd : annotated) {
            String clasPath = bd.getName();
            String clasName = bd.getSimpleName();
            String pk = null;
            for (Method method : bd.getMethods()) {
                String methodName = method.getName();
                method.getGenericParameterTypes();
                if (method.getParameterCount() == 0//
                        && Modifier.isPublic(method.getModifiers()) //
                        && methodName.startsWith("get")) {
                    if (methodName.length() <= 3)
                        continue;
                    if (method.getAnnotation(Id.class) != null) {
                        pk = method.getReturnType().getName();
                    }
                }

            }
            myEntities.add(new MyEntity(clasName, clasPath, pk));

        }

         String
                pathService = basePath + "/services",
                pathSVImpl = basePath + "/services/impl",
                pathSVAbs = basePath + "/services/abs",
                pathController = basePath + "/controller/impl",
                repo_path = basePath + "/dao";
        Files.createDirectories(Paths.get("./" + pathService));
        Files.createDirectories(Paths.get("./" + pathSVImpl));
        Files.createDirectories(Paths.get("./" + pathSVAbs));
        Files.createDirectories(Paths.get("./" + pathController));
        Files.createDirectories(Paths.get("./" + repo_path));
        GenSpringClass test = new GenSpringClass();
        try {
            test.createService(myEntities, pathService);
            test.createAbstract(myEntities, pathService, repo_path);
            test.createServiceImpl(myEntities, pathService);
            test.createController(myEntities, pathService, repo_path, pathController);
            test.createRepository(myEntities, repo_path);
        } catch (Exception e) {
            throw e;
        } finally {
        }
    }

    private void createRepository(List<MyEntity> entities, String repo_path) throws IOException {
        String content;
        for (MyEntity t : entities) {
            String tname = t.getClassName();
            String repoName = tname + "Repository";

            String pakage = repo_path.replaceAll("/", ".");
            //Repository
            content = "package " + pakage +
                    ";\n" +                            "\n\n\n\n   ////@Copyright by https://github.com/KP-story;\n" +

                    "import " + t.getFullName() + ";\n" +
                    "public interface " + repoName + " extends BaseRepo<" + tname + ", " + t.getPk() + "> {\n" +
                    "\n" +
                    "}";

            File file = new File("./" + repo_path + "/" + repoName + ".java");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println("File already exists.");
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
            } else {
                System.out.println(file.getName() + "  already exists.");

            }
        }
    }

    private void createService(List<MyEntity> entities, String repo_path) throws IOException {
   String content;
        for (MyEntity t : entities) {
            String tname = t.getClassName();
            String fileName = tname + "Service";
            String pakage = repo_path.replaceAll("/", ".");

            content = "package " + pakage +
                    ";\n" +                            "\n\n\n\n//@Copyright by https://github.com/KP-story;\n" +

                    "public interface " + fileName + " {\n" +
                    "}";
            File file = new File("./" + repo_path + "/" + fileName + ".java");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println("File already exists.");
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
            } else {
                System.out.println(file.getName() + "  already exists.");

            }
        }
    }

    private void createAbstract(List<MyEntity> entities, String service_path, String repo_path) throws IOException {
        String content;
        for (MyEntity t : entities) {
            String tname = t.getClassName();
            String fileName = tname + "Service";
            String fileNameAbs = "Abstract" + tname + "Service";
            String pakage = service_path.replaceAll("/", ".");
            String repoPk = repo_path.replaceAll("/", ".");
            content = "package " + pakage + ".abs;" +
                    "\n" +
                    "import " + t.getFullName() + " ;\n" +
                    "import " + repoPk + "." + t.getClassName() + "Repository;\n" +
                    "import com.kp.core.spring.admin.services.AbstractCRUDService;\n" +
                    "import " + pakage + "." + fileName + ";\n" +
                    "\n" +                            "\n\n\n\n//@Copyright by https://github.com/KP-story;\n" +

                    "public abstract class " + fileNameAbs + " extends AbstractCRUDService<" + tname + ", " + t.getPk() + ", " + tname + "Repository> implements " + fileName + " {\n" +
                    "}";

            File file = new File("./" + service_path + "/abs/" + fileNameAbs + ".java");
            if (!file.exists()) {

                if (!file.createNewFile()) {
                    System.out.println("File already exists.");
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
            } else {
                System.out.println(file.getName() + "  already exists.");

            }
        }
    }

    private void createServiceImpl(List<MyEntity> entities, String service_path) throws IOException {
        String content;
        for (MyEntity t : entities) {
            String tname = t.getClassName();
            String fileName = tname + "Service";
            String fileNameAbs = "Abstract" + tname + "Service";
            String fileImpl = fileName + "Impl";
            String servicePakage = service_path.replace("/", ".");
            content = "package  " + servicePakage + ".impl;" +
                    "\n" +
                    "import " + t.getFullName() + ";\n" +
                    "import " + servicePakage + ".abs." + fileNameAbs + ";\n" +
                    "import org.springframework.stereotype.Service;\n" +
                    "\n" +
                    "\n\n\n\n//@Copyright by https://github.com/KP-story;\n" +

                    "@Service\n" +
                    "public class " + fileImpl + " extends " + fileNameAbs + " {\n" +
                    "}";

            File file = new File("./" + service_path + "/impl/" + fileImpl + ".java");
            if (!file.exists()) {

                if (!file.createNewFile()) {
                    System.out.println("File already exists.");
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
            } else {
                System.out.println(file.getName() + "  already exists.");

            }
        }
    }

    private void createController(List<MyEntity> entities, String service_path, String repo_path, String pathController) throws IOException {
        String content;
        for (MyEntity t : entities) {

            String tname = t.getClassName();
            String controllerName = tname + "Controller";
            String packageControler = pathController.replaceAll("/", ".");
            String pakageRepo = repo_path.replaceAll("/", ".");
            String pakageService = service_path.replaceAll("/", ".");
            content = "package " + packageControler +
                    ";\n" +
                    "import com.kp.core.spring.admin.controller.BaseControler;\n" +
                    "import " + t.getFullName() + ";\n" +
                    "import " + pakageRepo + "." + tname + "Repository;\n" +
                    "import " + pakageService + ".abs.Abstract" + tname + "Service;\n" +
                    "import com.kp.core.spring.admin.vo.ResponseMsg;\n" +
                    "import org.springframework.data.domain.Pageable;\n" +
                    "import org.springframework.http.MediaType;\n" +
                    "import java.util.List;\n" +

                    "import org.springframework.web.bind.annotation.*;\n" +
                    "import javax.validation.Valid;\n" +
                            "\n\n\n\n//@Copyright by https://github.com/KP-story;\n" +

                    "\n" +
                    "@RestController\n" +
                    "@RequestMapping(\"/api/"+ WordUtils.uncapitalize(tname) +"\")\n" +
                    "public class " + controllerName + " extends BaseControler<" + tname + ", " + t.getPk() + ", " + tname + "Repository, Abstract" + tname + "Service> {\n" +
                    "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/list\", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg get" + tname + "s(Pageable pageable) throws Exception {\n" +
                    "        return findAll(pageable);\n" +
                    "    }\n" +
                    "\n" +
                    "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/{id}\", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg getDetail" + tname + "(@PathVariable(\"id\") " + t.getPk() + " id) throws Exception {\n" +
                    "        return super.getById(id);\n" +
                    "    }" +
                    "\n" +
                    "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/\", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg add" + tname + "(@Valid @RequestBody " + tname + " " + WordUtils.uncapitalize(tname) + ") {\n" +
                    "        return create(" + WordUtils.uncapitalize(tname) + ");\n" +
                    "    }\n" +
                    "\n" +
                    "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/{id}\", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg update" + tname + "(@PathVariable(\"id\") " + t.getPk() + " id, @Valid @RequestBody " + tname + " " + WordUtils.uncapitalize(tname) + ") throws Exception {\n" +
                    "        return super.update(id, " + WordUtils.uncapitalize(tname) + ");\n" +
                    "    }" +
                    "\n" +
                    "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/{id}\", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg delete" + tname + "(@PathVariable(\"id\") " + t.getPk() + " id) {\n" +
                    "        return super.delete(id);\n" +
                    "    }" +
                    "\n" +

                            "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/search\", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg search" + tname + "(Pageable pageable, " + tname + " " + WordUtils.uncapitalize(tname) +  ") throws Exception {\n" +
                    "                return findByObject("+WordUtils.uncapitalize(tname) +",pageable);\n\n" +
                    "    }" +
                    "\n" +

                            "    @CrossOrigin(origins = \"/**\")\n" +
                    "    @RequestMapping(value = \"/multiple/delete\", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})\n" +
                    "    @ResponseBody\n" +
                    "    public ResponseMsg deleteMulti" + tname + "(@RequestBody List<"+tname+"> "+WordUtils.uncapitalize(tname)+"  )throws Exception {\n" +
                    "                return deleteMultiInBatch("+WordUtils.uncapitalize(tname)+");\n\n" +
                    "    }" +
                    "\n" +
                    "    @Override\n" +
                    "    public void merge(" + tname + " newBean, " + tname + " currentBean) {\n" +
                    "        //toDo: Cap nhat thong tin bean thay doi (phai tu code)\n" +
                    "    }\n" +
                    "    \n" +
                    "    @Override\n" +
                    "    public String getBeanName() {\n" +
                    "        return \"" + WordUtils.uncapitalize(tname) + "\";\n" +
                    "    }\n" +
                    "} ";
            File file = new File("./" + pathController + "/" + controllerName + ".java");
            if (!file.exists()) {

                if (!file.createNewFile()) {
                    System.out.println("File already exists.");
                }
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
            } else {
                System.out.println(file.getName() + "  already exists.");

            }
        }
    }


    public static class MyEntity {
        String className;
        String fullName;
        String pk;

        public MyEntity(String className, String fullName, String pk) {
            this.className = className;
            this.fullName = fullName;
            this.pk = pk;
            System.out.println(this.toString());
        }

        @Override
        public String toString() {
            return "MyEntity{" +
                    "className='" + className + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", pk='" + pk + '\'' +
                    '}';
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPk() {
            return pk;
        }

        public void setPk(String pk) {
            this.pk = pk;
        }


    }
}
