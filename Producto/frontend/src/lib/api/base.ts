const trimTrailingSlash = (value: string | undefined): string | undefined =>
  value?.replace(/\/$/, '');

const gatewayBaseUrl = trimTrailingSlash(process.env.NEXT_PUBLIC_API_URL);

export function getServiceBaseUrl(envVar: string, localDefault: string): string {
  let value: string | undefined;

  switch (envVar) {
    case 'NEXT_PUBLIC_API_URL':
      value = process.env.NEXT_PUBLIC_API_URL;
      break;
    case 'NEXT_PUBLIC_NOTIFICATION_SERVICE_URL':
      value = process.env.NEXT_PUBLIC_NOTIFICATION_SERVICE_URL;
      break;
    case 'NEXT_PUBLIC_PROFILE_SERVICE_URL':
      value = process.env.NEXT_PUBLIC_PROFILE_SERVICE_URL;
      break;
    case 'NEXT_PUBLIC_AUTH_SERVICE_URL':
      value = process.env.NEXT_PUBLIC_AUTH_SERVICE_URL;
      break;
    case 'NEXT_PUBLIC_JOBS_SERVICE_URL':
      value = process.env.NEXT_PUBLIC_JOBS_SERVICE_URL;
      break;
    case 'NEXT_PUBLIC_MATCHING_SERVICE_URL':
      value = process.env.NEXT_PUBLIC_MATCHING_SERVICE_URL;
      break;
    default:
      value = undefined;
  }

  return trimTrailingSlash(value) ?? gatewayBaseUrl ?? localDefault;
}

export function getNotificationBaseUrl(): string {
  return (
    trimTrailingSlash(process.env.NEXT_PUBLIC_NOTIFICATION_SERVICE_URL) ??
    gatewayBaseUrl ??
    'http://localhost:8088'
  );
}
