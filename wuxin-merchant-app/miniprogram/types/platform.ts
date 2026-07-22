export interface PlatformConfig { configKey: string; configValue: string; valueType: string; }
export interface PlatformNotice { id: number; title: string; content: string; noticeType: string; }
export interface PlatformHome {
  banners: unknown[];
  recommendations: unknown[];
  notices: PlatformNotice[];
  configs: PlatformConfig[];
}
