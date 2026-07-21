type PublicType = 'missing' | 'rescue' | 'bullying';

const CONTENT: Record<PublicType, { title: string; label: string; intro: string; steps: string[]; notice: string }> = {
  missing: { title: '寻找走失儿童', label: '公益寻人', intro: '传播经过核实的寻人信息，同时保护儿童与家庭隐私。', steps: ['遇到疑似走失儿童时，优先报警并确保自身安全', '不擅自拍摄、传播儿童正脸和家庭敏感信息', '只转发来源明确、仍在有效期内的寻人信息'], notice: '本页面不发布真实儿童个人信息，也不接收寻人资料。紧急情况请立即联系公安机关。' },
  rescue: { title: '免费紧急救援', label: '紧急协助', intro: '为突发情形提供清晰的安全处置指引。', steps: ['生命安全受到威胁时立即拨打 110 或 120', '说明所在位置、现场情况与联系方式', '等待专业人员期间避免进入危险区域'], notice: '五鑫当前未开通线上救援派单，本页面不能替代公安、消防和医疗急救。' },
  bullying: { title: '免费反校园欺凌', label: '安全守护', intro: '帮助未成年人识别欺凌、保存证据并寻求可信支持。', steps: ['尽快离开危险环境，不独自对抗多人威胁', '向家长、老师或学校管理人员明确说明情况', '保留聊天、录音、照片等证据，必要时报警'], notice: '请勿在公共页面发布未成年人姓名、学校、住址或影像。持续受到威胁时应由监护人陪同报警。' }
};

Page({
  data: { activeType: 'missing' as PublicType, content: CONTENT.missing },
  onLoad(options: { type?: string }) { this.applyType(normalizeType(options.type)); },
  selectType(event: WechatMiniprogram.BaseEvent) { this.applyType(normalizeType(String(event.currentTarget.dataset.type))); },
  applyType(type: PublicType) { this.setData({ activeType: type, content: CONTENT[type] }); }
});

function normalizeType(value?: string): PublicType { return value === 'rescue' || value === 'bullying' ? value : 'missing'; }
