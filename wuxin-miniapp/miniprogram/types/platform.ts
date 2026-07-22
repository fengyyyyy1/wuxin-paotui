export interface PlatformBanner {
  id: number;
  title: string;
  subtitle: string | null;
  imageUrl: string;
  targetType: 'NONE' | 'STORE' | 'PRODUCT' | 'PAGE' | 'URL';
  targetValue: string | null;
  sort: number;
  status: number;
}

export interface PlatformNotice {
  id: number;
  noticeType: string;
  title: string;
  content: string;
  publishTime: string | null;
}

export interface PlatformRecommendation {
  id: number;
  recommendationType: string;
  targetId: number;
  targetName: string;
  titleOverride: string | null;
  sort: number;
}

export interface PlatformConfig {
  id: number;
  configGroup: string;
  configKey: string;
  configValue: string;
  valueType: string;
  configName: string;
}

export interface PlatformHome {
  banners: PlatformBanner[];
  notices: PlatformNotice[];
  recommendations: PlatformRecommendation[];
  configs: PlatformConfig[];
}
