import type { Address } from '../types/address';

export function buildFullAddress(address: Address): string {
  return [address.province, address.city, address.district, address.detailAddress]
    .filter(Boolean)
    .join('');
}

export function buildAddressSummary(address?: Address | null): string {
  if (!address) {
    return '请选择收货地址';
  }

  const district = address.district || '';
  const detail = address.detailAddress || '';
  const summary = `${district}${detail}`;
  if (!summary) {
    return buildFullAddress(address) || '请选择收货地址';
  }
  return summary.length > 22 ? `${summary.slice(0, 22)}...` : summary;
}

export function findDefaultAddress(addresses: Address[]): Address | null {
  return addresses.find((address) => address.isDefault === 1) || addresses[0] || null;
}
