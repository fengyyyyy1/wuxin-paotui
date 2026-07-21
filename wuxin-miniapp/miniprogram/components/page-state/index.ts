Component({
  properties: {
    loading: { type: Boolean, value: false },
    error: { type: String, value: '' },
    empty: { type: Boolean, value: false },
    emptyTitle: { type: String, value: '暂无内容' },
    emptyDescription: { type: String, value: '这里暂时还没有数据' },
    actionText: { type: String, value: '' }
  },
  methods: {
    handleRetry() {
      this.triggerEvent('retry');
    },
    handleAction() {
      this.triggerEvent('action');
    }
  }
});
