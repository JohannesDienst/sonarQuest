export interface SonarCubeConfig {
  name: string;
  sonarServerUrl: string;
  httpBasicAuthUsername?: string;
  httpBasicAuthPassword?: string;
  proxy?: string;
  proxyPort?: string;
}
