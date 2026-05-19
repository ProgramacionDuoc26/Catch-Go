const trimTrailingSlash = (value: string | undefined): string | undefined =>
  value?.replace(/\/$/, '');

// Dynamically deduce the API Gateway domain in production if NEXT_PUBLIC_API_URL is missing/undefined in the browser bundle
export function getGatewayBaseUrl(): string {
  if (typeof window !== 'undefined') {
    if (process.env.NEXT_PUBLIC_API_URL) {
      return trimTrailingSlash(process.env.NEXT_PUBLIC_API_URL)!;
    }

    const host = window.location.hostname;
    if (host.endsWith('.up.railway.app')) {
      // Find the app prefix and replace it with the api-gateway prefix
      const prefix = host.substring(0, host.indexOf('.up.railway.app'));
      const newPrefix = prefix.replace(/(catch-go|catchgo|frontend)/i, 'api-gateway');
      return `https://${newPrefix}.up.railway.app`;
    }
  }
  return trimTrailingSlash(process.env.NEXT_PUBLIC_API_URL) ?? 'http://localhost:8080';
}

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

  return trimTrailingSlash(value) ?? getGatewayBaseUrl() ?? localDefault;
}

export function getNotificationBaseUrl(): string {
  return (
    trimTrailingSlash(process.env.NEXT_PUBLIC_NOTIFICATION_SERVICE_URL) ??
    getGatewayBaseUrl() ??
    'http://localhost:8088'
  );
}
