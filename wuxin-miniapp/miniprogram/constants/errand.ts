export type ErrandServiceType =
  'send' | 'buy' | 'pickup' | 'handle' | 'queue' | 'print' | 'flower' | 'pet' | 'more';

export interface ErrandServiceEntry {
  id: ErrandServiceType;
  title: string;
  description: string;
  iconText: string;
}

export const ERRAND_SERVICES: ErrandServiceEntry[] = [
  { id: 'send', title: '帮我送', description: '同城急送', iconText: '送' },
  { id: 'buy', title: '帮我买', description: '代买代购', iconText: '买' },
  { id: 'pickup', title: '帮我取', description: '快递代取', iconText: '取' },
  { id: 'handle', title: '帮我办', description: '排队办事', iconText: '办' },
  { id: 'queue', title: '代排队', description: '节省时间', iconText: '排' },
  { id: 'print', title: '文件打印', description: '打印配送', iconText: '印' },
  { id: 'flower', title: '鲜花配送', description: '心意送达', iconText: '花' },
  { id: 'pet', title: '宠物代遛', description: '安心陪伴', iconText: '宠' },
  { id: 'more', title: '更多服务', description: '更多需求', iconText: '全' }
];

export const HOME_ERRAND_SERVICES: ErrandServiceEntry[] = [
  ERRAND_SERVICES[0],
  ERRAND_SERVICES[1],
  ERRAND_SERVICES[2],
  ERRAND_SERVICES[3],
  { id: 'more', title: '更多跑腿', description: '全部服务', iconText: '全' }
];

export function findErrandService(type?: string): ErrandServiceEntry {
  return ERRAND_SERVICES.find((service) => service.id === type) || ERRAND_SERVICES[0];
}
