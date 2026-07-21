import { ROUTES } from './routes';

export interface HomeBanner {
  id: string;
  title: string;
  subtitle: string;
  imageUrl: string;
  target?: string;
}

export interface HomeEntry {
  id: string;
  title: string;
  description: string;
  iconText: string;
  target: string;
}

export interface PublicEntry {
  id: 'missing' | 'rescue' | 'bullying';
  title: string;
  label: string;
  description: string;
  accent: string;
}

export const HOME_BANNERS: HomeBanner[] = [
  { id: 'brand', title: '五鑫跑腿', subtitle: '本地生活好物，安心送到身边', imageUrl: '/assets/images/home/banner-brand.svg', target: ROUTES.storeList },
  { id: 'campus', title: '便捷生活服务', subtitle: '精选附近门店，发现日常所需', imageUrl: '/assets/images/home/banner-campus.svg', target: ROUTES.search },
  { id: 'public', title: '五鑫公益', subtitle: '让每一份需要都被认真看见', imageUrl: '/assets/images/home/banner-public.svg', target: `${ROUTES.publicService}?type=missing` }
];

export const SERVICE_ENTRIES: HomeEntry[] = [
  { id: 'stores', title: '附近门店', description: '本地好店', iconText: '店', target: ROUTES.storeList },
  { id: 'search', title: '搜索商品', description: '快速发现', iconText: '搜', target: ROUTES.search },
  { id: 'cart', title: '购物车', description: '已选好物', iconText: '篮', target: ROUTES.cart },
  { id: 'address', title: '地址管理', description: '配送地址', iconText: '址', target: ROUTES.addressList }
];

export const PUBLIC_ENTRIES: PublicEntry[] = [
  { id: 'missing', title: '寻找走失儿童', label: '公益寻人', description: '了解信息发布与隐私保护规范', accent: 'green' },
  { id: 'rescue', title: '免费紧急救援', label: '紧急协助', description: '查看紧急情形下的求助指引', accent: 'orange' },
  { id: 'bullying', title: '免费反校园欺凌', label: '安全守护', description: '获取保护自己与求助的方式', accent: 'blue' }
];
