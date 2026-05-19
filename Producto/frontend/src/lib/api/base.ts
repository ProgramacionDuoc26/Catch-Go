const trimTrailingSlash = (value: string | undefined): string | undefined =>
  value?.replace(/\/$/, '');

const env = process.env as Record<string, string | undefined>;

const gatewayBaseUrl = trimTrailingSlash(env.NEXT_PUBLIC_API_URL);

export function getServiceBaseUrl(envVar: string, localDefault: string): string {
  return trimTrailingSlash(env[envVar]) ?? gatewayBaseUrl ?? localDefault;
}

export function getNotificationBaseUrl(): string {
  return (
    trimTrailingSlash(env.NEXT_PUBLIC_NOTIFICATION_SERVICE_URL) ??
    gatewayBaseUrl ??
    'http://localhost:8088'
  );
}
