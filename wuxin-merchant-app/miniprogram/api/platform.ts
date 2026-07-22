import type { PlatformHome } from '../types/platform';
import { request } from '../utils/request';

export function getPlatformHome(): Promise<PlatformHome> {
  return request<PlatformHome>({ url: '/api/platform/home', auth: false });
}
