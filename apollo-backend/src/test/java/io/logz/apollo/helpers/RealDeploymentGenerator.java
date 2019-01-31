package io.logz.apollo.helpers;

import io.logz.apollo.models.User;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.dao.UserDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;

/**
 * Created by roiravhon on 2/7/17.
 */
public class RealDeploymentGenerator {

    private static final int NODE_PORT = 30002;
    private final int SERVICE_PORT = 80;
    private final String SERVICE_NAME = "test-service";
    private final String DEFAULT_LABEL_KEY = "app";
    private final String DEFAULT_LABEL_VALUE = "nginx";

    private final String DEFAULT_ENVIRONMENT_VARIABLE_NAME = "ENV";
    private final String DEFAULT_ENVIRONMENT_VARIABLE_VALUE = "enval";

    public static final String DEFAULT_DEPLOYMENT_LABLE_KEY = "deployment_key";
    public static final String DEFAULT_SERVICE_LABLE_KEY = "service_key";
    public static final String DEFAULT_INGRESS_LABLE_KEY = "ingress_key";
    private final String DEFAULT_DEPLOYMENT_LABLE_VALUE = "{{ deployment_value }}";
    private final String DEFAULT_SERVICE_LABLE_VALUE = "{{ service_value }}";
    private final String DEFAULT_INGRESS_LABLE_VALUE = "{{ ingress_value }}";

    private final StandaloneApollo standaloneApollo;
    private final Service service;
    private final DeployableVersion deployableVersion;
    private final User user;
    private final Deployment deployment;
    private Environment environment;

    public RealDeploymentGenerator(String deploymentImageName, String extraLabelKey, String extraLabelValue, int servicePortCoefficient) {
        this(deploymentImageName, extraLabelKey, extraLabelValue, servicePortCoefficient, null, null, null, null);
    }

    public RealDeploymentGenerator(String deploymentImageName, String extraLabelKey, String extraLabelValue, int servicePortCoefficient, String deploymentParams) {
        this(deploymentImageName, extraLabelKey, extraLabelValue, servicePortCoefficient, deploymentParams, null, null, null);
    }

    public RealDeploymentGenerator(String deploymentImageName, String extraLabelKey, String extraLabelValue,
                                   int servicePortCoefficient, String deploymentParams, Service serviceParam, Environment environmentParam, String groupName) {
        this(deploymentImageName, extraLabelKey, extraLabelValue, servicePortCoefficient, deploymentParams, serviceParam, environmentParam, groupName, false);
    }

    public RealDeploymentGenerator(String deploymentImageName, String extraLabelKey, String extraLabelValue,
                                   int servicePortCoefficient, String deploymentParams, Service serviceParam, Environment environmentParam, String groupName, Boolean defaultYamlsLabels) {
        try {
            standaloneApollo = StandaloneApollo.getOrCreateServer();
            DeploymentDao deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
            EnvironmentDao environmentDao = standaloneApollo.getInstance(EnvironmentDao.class);
            ServiceDao serviceDao = standaloneApollo.getInstance(ServiceDao.class);
            DeployableVersionDao deployableVersionDao = standaloneApollo.getInstance(DeployableVersionDao.class);
            UserDao userDao = standaloneApollo.getInstance(UserDao.class);

            // Create all models in DB
            if (environmentParam == null) {
                environment = ModelsGenerator.createEnvironment();
                environment.setServicePortCoefficient(servicePortCoefficient);
                environmentDao.addEnvironment(environment);
            } else {
                environment = environmentParam;
            }

            if (serviceParam == null) {
                service = ModelsGenerator.createService();
                if (defaultYamlsLabels) {
                    if (groupName != null) {
                        service.setDeploymentYaml(getDeploymentKubernetesYaml(deploymentImageName, DEFAULT_DEPLOYMENT_LABLE_KEY,
                                DEFAULT_DEPLOYMENT_LABLE_VALUE, extraLabelKey, extraLabelValue));
                    } else {
                        service.setDeploymentYaml(getDeploymentKubernetesYaml(deploymentImageName, DEFAULT_DEPLOYMENT_LABLE_KEY, DEFAULT_DEPLOYMENT_LABLE_VALUE));
                    }
                    service.setServiceYaml(getServiceDeploymentYaml(DEFAULT_SERVICE_LABLE_KEY, DEFAULT_SERVICE_LABLE_VALUE));
                    service.setIngressYaml(getIngressServiceYaml(DEFAULT_INGRESS_LABLE_KEY, DEFAULT_INGRESS_LABLE_VALUE));
                } else {
                    service.setDeploymentYaml(getDeploymentKubernetesYaml(deploymentImageName, extraLabelKey, extraLabelValue));
                    service.setServiceYaml(getServiceDeploymentYaml(extraLabelKey, extraLabelValue));
                    service.setIngressYaml(getIngressServiceYaml(extraLabelKey, extraLabelValue));
                }
                serviceDao.addService(service);
            } else {
                if (defaultYamlsLabels) {
                    if (groupName != null) {
                        serviceParam.setDeploymentYaml(getDeploymentKubernetesYaml(deploymentImageName, DEFAULT_DEPLOYMENT_LABLE_KEY,
                                DEFAULT_DEPLOYMENT_LABLE_VALUE, extraLabelKey, extraLabelValue));
                    } else {
                        serviceParam.setDeploymentYaml(getDeploymentKubernetesYaml(deploymentImageName, DEFAULT_DEPLOYMENT_LABLE_KEY, DEFAULT_DEPLOYMENT_LABLE_VALUE));
                    }
                    serviceParam.setServiceYaml(getServiceDeploymentYaml(DEFAULT_SERVICE_LABLE_KEY, DEFAULT_SERVICE_LABLE_VALUE));
                    serviceParam.setIngressYaml(getIngressServiceYaml(DEFAULT_INGRESS_LABLE_KEY, DEFAULT_INGRESS_LABLE_VALUE));
                } else {
                    serviceParam.setDeploymentYaml(getDeploymentKubernetesYaml(deploymentImageName, extraLabelKey, extraLabelValue));
                    serviceParam.setServiceYaml(getServiceDeploymentYaml(extraLabelKey, extraLabelValue));
                    serviceParam.setIngressYaml(getIngressServiceYaml(extraLabelKey, extraLabelValue));
                }
                serviceDao.updateService(serviceParam);
                service = serviceDao.getService(serviceParam.getId());
            }

            deployableVersion = ModelsGenerator.createDeployableVersion(service);
            deployableVersionDao.addDeployableVersion(deployableVersion);

            user = ModelsGenerator.createRegularUser();
            userDao.addUser(user);

            deployment = ModelsGenerator.createDeployment(service, environment, deployableVersion);
            deployment.setStatus(Deployment.DeploymentStatus.PENDING);
            deployment.setSourceVersion("abc" + Common.randomStr(5));
            deployment.setUserEmail(user.getUserEmail());
            deployment.setDeploymentParams(deploymentParams);
            deployment.setGroupName(groupName);
            deploymentDao.addDeployment(deployment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDefaultLabelKey() {
        return DEFAULT_LABEL_KEY;
    }

    public String getDefaultLabelValue() {
        return DEFAULT_LABEL_VALUE;
    }

    public String getDefaultEnvironmentVariableName() {
        return DEFAULT_ENVIRONMENT_VARIABLE_NAME;
    }

    public String getDefaultEnvironmentVariableValue() {
        return DEFAULT_ENVIRONMENT_VARIABLE_VALUE;
    }

    public int getDefaultNodePort() {
        return NODE_PORT;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) { this.environment = environment; }

    public Service getService() {
        return service;
    }

    public DeployableVersion getDeployableVersion() {
        return deployableVersion;
    }

    public User getUser() {
        return user;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void updateDeploymentStatus(Deployment.DeploymentStatus deploymentStatus) {
        DeploymentDao deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
        deploymentDao.updateDeploymentStatus(deployment.getId(), deploymentStatus);
    }

    private String getDeploymentKubernetesYaml(String imageName, String extraLabelKey, String extraLabelValue) {
        return getDeploymentKubernetesYaml(imageName, extraLabelKey, extraLabelValue, null, null);
    }

    private String getDeploymentKubernetesYaml(String imageName, String extraLabelKey, String extraLabelValue,
                                               String secondExtraLabelKey, String secondExtraLabelValue) {

        String deploymentYaml = "apiVersion: extensions/v1beta1\n" +
                "kind: Deployment\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    name: nginx\n" +
                "    tahat: nginx\n" +
                "    " + DEFAULT_LABEL_KEY + ": " + DEFAULT_LABEL_VALUE + "\n" +
                "    " + extraLabelKey + ": " + extraLabelValue + "\n";

        if (secondExtraLabelKey != null && secondExtraLabelValue != null) {
            deploymentYaml += "    " + secondExtraLabelKey + ": " + secondExtraLabelValue + "\n";
        }

        deploymentYaml +=
                "  name: nginx\n" +
                "  namespace: default\n" +
                "spec:\n" +
                "  replicas: 1\n" +
                "  strategy:\n" +
                "    rollingUpdate:\n" +
                "      maxSurge: 1\n" +
                "      maxUnavailable: 0\n" +
                "    type: RollingUpdate\n" +
                "  template:\n" +
                "    metadata:\n" +
                "      labels:\n" +
                "        pod: label\n" +
                "    spec:\n" +
                "      containers:\n" +
                "      - image: " + imageName + "\n" +
                "        imagePullPolicy: Always\n" +
                "        name: roi-apollo-test\n" +
                "        env:\n" +
                "        -  name: " + DEFAULT_ENVIRONMENT_VARIABLE_NAME + "\n" +
                "           value: " + DEFAULT_ENVIRONMENT_VARIABLE_VALUE + "\n" +
                "        ports:\n" +
                "        - containerPort: 80\n" +
                "          protocol: TCP\n" +
                "        resources: {}\n" +
                "      dnsPolicy: ClusterFirst\n" +
                "      restartPolicy: Always\n" +
                "      securityContext: {}\n" +
                "      terminationGracePeriodSeconds: 30";

        return deploymentYaml;
    }

    private String getServiceDeploymentYaml(String extraLabelKey, String extraLabelValue) {
        return "apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    " + DEFAULT_LABEL_KEY + ": " + DEFAULT_LABEL_VALUE + "\n" +
                "    " + extraLabelKey + ": " + extraLabelValue + "\n" +
                "  name: " + SERVICE_NAME +"\n" +
                "  namespace: default\n" +
                "spec:  \n" +
                "  ports:\n" +
                "  - nodePort: " + NODE_PORT + "\n" +
                "    port: "+ SERVICE_PORT +"\n" +
                "    protocol: TCP\n" +
                "    targetPort: 80\n" +
                "  selector:\n" +
                "    app: nginx\n" +
                "  sessionAffinity: None\n" +
                "  type: NodePort\n" +
                "status:\n" +
                "  loadBalancer: {}";
    }

    private String getIngressServiceYaml(String extraLabelKey, String extraLabelValue) {
        return "apiVersion: extensions/v1beta1\n" +
                "kind: Ingress\n" +
                "metadata:\n" +
                "  labels:\n" +
                "    " + DEFAULT_LABEL_KEY + ": " + DEFAULT_LABEL_VALUE + "\n" +
                "    " + extraLabelKey + ": " + extraLabelValue + "\n" +
                "  name: ingress-test\n" +
                "  namespace: default\n" +
                "  annotations:\n" +
                "    kubernetes.io/ingress.class: traefik\n" +
                "spec:  \n" +
                "  rules:\n" +
                "  - host: app.example.hostname\n" +
                "    http:\n" +
                "       paths:\n" +
                "           - backend:\n" +
                "               serviceName: " + SERVICE_NAME +"\n" +
                "               servicePort: " + SERVICE_PORT +"\n";
    }
}
