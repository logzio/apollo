package io.logz.apollo;

import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by roiravhon on 1/15/17.
 */
public class TestGround {

    // This is a very important test comment.

    @Test
    public void testGround() throws IOException, ScriptException, SQLException {

//        String kubeMaster = "https://172.31.24.6:6443";
//        String oauthToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImFwb2xsby10b2tlbi1pZHZ4OCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJhcG9sbG8iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI5MDAwYjA1ZS1hY2MwLTExZTYtOWI1ZC0wYTc3MGIxYWQ4NDAiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6ZGVmYXVsdDphcG9sbG8ifQ.HNzku2KCbqClA_APVWsS_r4NxAS_Kf9-2eW770PJTT7buoAdJNLMjgObAyc4-Lh0D6BRzkcY8JmCsILcnisgy5OWmoY86qV1011mYIb-_SINa11O8TpEraqYbLZGk2GbQCJaXUzlyzvaXx2eJoZ1XABhDWLp7D4JdM0-ac4kY9_vKcVGnosrB8-zIMqWZVjd467wKyv78-kG3uQr2mkP4ZKY-JKAxkcK_WOC-AgsRfN9ekPfeSCFj3gqrRB5_ep9bLPURf8sEcdb3LYwb81QH0S5-2ZqNM170gWsGv5tUvBXXhox0J9FQm1u9J9EyD3tPiDBB6x7OyyyoT7x64bDyA";
//        Config config = new ConfigBuilder().withMasterUrl(kubeMaster).withOauthToken(oauthToken).build();
//        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);

        String depyaml = "apiVersion: extensions/v1beta1\n" +
                "kind: Deployment\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    app: nginx\n" +
                "  name: nginx\n" +
                "  namespace: default\n" +
                "spec:\n" +
                "  replicas: 1\n" +
                "  selector:\n" +
                "    matchLabels:\n" +
                "      app: nginx\n" +
                "  strategy:\n" +
                "    rollingUpdate:\n" +
                "      maxSurge: 1\n" +
                "      maxUnavailable: 0\n" +
                "    type: RollingUpdate\n" +
                "  template:\n" +
                "    metadata:\n" +
                "      labels:\n" +
                "        app: nginx\n" +
                "    spec:\n" +
                "      containers:\n" +
                "      - image: registry.internal.logz.io:5000/roi-sample-app\n" +
                "        imagePullPolicy: Always\n" +
                "        name: roi-apollo-test\n" +
                "        ports:\n" +
                "        - containerPort: 80\n" +
                "          protocol: TCP\n" +
                "        resources: {}\n" +
                "      dnsPolicy: ClusterFirst\n" +
                "      restartPolicy: Always\n" +
                "      securityContext: {}\n" +
                "      terminationGracePeriodSeconds: 30";

        String seryaml = "apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    app: nginx\n" +
                "  name: roi-test-service\n" +
                "  namespace: default\n" +
                "spec:  \n" +
                "  ports:\n" +
                "  - nodePort: 30002\n" +
                "    port: 80\n" +
                "    protocol: TCP\n" +
                "    targetPort: 80\n" +
                "  selector:\n" +
                "    app: nginx\n" +
                "  sessionAffinity: None\n" +
                "  type: NodePort\n" +
                "status:\n" +
                "  loadBalancer: {}";


//        kubernetesClient
//                .pods()
//                .inNamespace("default")
//                .withLabel("apollo_unique_identifier", "apollo_deployment_env-name-62e05_Prod_app_e90d6")
//                .list()
//                .getItems()
//                .stream()
//                .map(pod -> pod.getMetadata().getName())
//                .forEach(name -> {
//                    System.out.println("Logs from " + name + ":");
//                    System.out.println(kubernetesClient.pods().inNamespace("default").withName(name).getLog(true));
//                });


//        StandaloneApollo.getOrCreateServer();
//
//        // Get the DAOs we need
//        EnvironmentDao environmentDao = ApolloMyBatis.getDao(EnvironmentDao.class);
//        ServiceDao serviceDao = ApolloMyBatis.getDao(ServiceDao.class);
//        DeployableVersionDao deployableVersionDao = ApolloMyBatis.getDao(DeployableVersionDao.class);
//        UserDao userDao = ApolloMyBatis.getDao(UserDao.class);
//        DeploymentDao deploymentDao = ApolloMyBatis.getDao(DeploymentDao.class);
//
//        Environment testEnvironment = ModelsGenerator.createEnvironment();
//        testEnvironment.setKubernetesNamespace("default");
//        testEnvironment.setKubernetesToken(oauthToken);
//        testEnvironment.setKubernetesMaster(kubeMaster);
//        environmentDao.addEnvironment(testEnvironment);
//
//        Service testService = ModelsGenerator.createService();
//        testService.setDeploymentYaml(depyaml);
//        testService.setServiceYaml(seryaml);
//        serviceDao.addService(testService);
//
//        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
//        testDeployableVersion.setGitCommitSha("1");
//        deployableVersionDao.addDeployableVersion(testDeployableVersion);
//
//        User testUser = ModelsGenerator.createRegularUser();
//        userDao.addUser(testUser);
//
//        Deployment testDeployment = ModelsGenerator.createDeployment(testService, testEnvironment, testDeployableVersion, testUser);
//        testDeployment.setStatus(Deployment.DeploymentStatus.PENDING);
//        deploymentDao.addDeployment(testDeployment);
//
//        KubernetesHandler kubernetesHandler = KubernetesHandlerStore.getOrCreateApolloToKubernetes(testEnvironment);
//        Deployment returnedDep = kubernetesHandler.startDeployment(testDeployment);


    }
}
