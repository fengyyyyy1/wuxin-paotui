export interface HomeBanner {
  id: string;
  title: string;
  subtitle: string;
  imageUrl: string;
  actionType: 'toast' | 'page';
  target?: string;
}

export interface HomeServiceEntry {
  id: string;
  title: string;
  description: string;
  iconText: string;
  actionType: 'store' | 'toast';
}

export interface PublicEntry {
  id: 'missing' | 'bullying' | 'rescue';
  title: string;
  tag: string;
  description: string;
  route: string;
}

export const HOME_BANNERS: HomeBanner[] = [
  {
    id: 'brand',
    title: '五鑫跑腿',
    subtitle: '校园生活服务，更快一步到身边',
    imageUrl: '/assets/images/home/banner-brand.svg',
    actionType: 'toast'
  },
  {
    id: 'campus',
    title: '便捷校园生活',
    subtitle: '代取、帮买、配送入口逐步开放',
    imageUrl: '/assets/images/home/banner-campus.svg',
    actionType: 'toast'
  },
  {
    id: 'public',
    title: '公益信息',
    subtitle: '走失儿童公益宣传与免费求助入口',
    imageUrl: '/assets/images/home/banner-public.svg',
    actionType: 'page',
    target: ROUTES.publicMissing
  }
];

export const SERVICE_ENTRIES: HomeServiceEntry[] = [
  {
    id: 'pickup',
    title: '跑腿代取',
    description: '快递、资料、物品代取',
    iconText: '取',
    actionType: 'toast'
  },
  {
    id: 'store',
    title: '商品配送',
    description: '浏览已入驻门店',
    iconText: '店',
    actionType: 'store'
  },
  {
    id: 'buy',
    title: '帮买服务',
    description: '超市、日用品帮买',
    iconText: '买',
    actionType: 'toast'
  },
  {
    id: 'send',
    title: '帮送服务',
    description: '同城物品帮送',
    iconText: '送',
    actionType: 'toast'
  }
];

export const PUBLIC_ENTRIES: PublicEntry[] = [
  {
    id: 'missing',
    title: '走失儿童公益信息',
    tag: '公益信息',
    description: '当前为本地静态展示数据，不展示真实个人敏感信息。',
    route: ROUTES.publicMissing
  },
  {
    id: 'bullying',
    title: '校园欺凌免费求助',
    tag: '免费求助',
    description: '提供说明页与安全提醒，暂不收集真实求助内容。',
    route: ROUTES.publicBullying
  },
  {
    id: 'rescue',
    title: '紧急免费救援',
    tag: '紧急免费救援',
    description: '试运营范围说明与紧急电话提醒，不提交救援订单。',
    route: ROUTES.publicRescue
  }
];
import { ROUTES } from './routes';
