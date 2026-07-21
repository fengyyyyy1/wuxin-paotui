export const DEFAULT_AVATAR = '/assets/images/default-avatar.svg';
export const DEFAULT_STORE_IMAGE = '/assets/images/home/store-placeholder.svg';
export const DEFAULT_PRODUCT_IMAGE = '/assets/images/product-placeholder.svg';

export function normalizeImageUrl(value: string | null | undefined, fallback: string): string {
  const imageUrl = value?.trim();
  if (!imageUrl || isUnsafeLocalPath(imageUrl) || isBlockedRemotePlaceholder(imageUrl)) {
    return fallback;
  }

  if (/^https?:\/\//i.test(imageUrl)) {
    return imageUrl;
  }

  if (imageUrl.startsWith('/miniprogram/assets/images/')) {
    return imageUrl.replace('/miniprogram', '');
  }

  if (imageUrl.startsWith('/pages/')) {
    return fallback;
  }

  if (imageUrl.startsWith('/')) {
    return imageUrl;
  }

  if (imageUrl.startsWith('assets/images/')) {
    return `/${imageUrl}`;
  }

  if (imageUrl.startsWith('miniprogram/assets/images/')) {
    return imageUrl.replace('miniprogram', '');
  }

  return fallback;
}

function isUnsafeLocalPath(value: string): boolean {
  return /^[a-zA-Z]:[\\/]/.test(value) || value.startsWith('file:') || value.includes('\\');
}

function isBlockedRemotePlaceholder(value: string): boolean {
  if (!/^https?:\/\//i.test(value)) {
    return false;
  }
  const matched = value.match(/^https?:\/\/([^/:?#]+)/i);
  if (!matched) {
    return true;
  }
  const hostname = matched[1].toLowerCase();
  const blockedHosts = ['localhost', '127.0.0.1'];
  const blockedSecondLevelDomains = ['example', 'test'];
  return (
    blockedHosts.includes(hostname) ||
    blockedSecondLevelDomains.some((domain) => hostname === `${domain}.com` || hostname === `www.${domain}.com`)
  );
}
