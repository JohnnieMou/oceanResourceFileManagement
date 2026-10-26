<template>
  <div class="home">
    <div class="topbarBox">
      <my-tabbar-vue></my-tabbar-vue>
    </div>
    <div class="map-container" ref="baiduMap"></div>
  </div>
</template>
<script>
/* global BMapGL */
export default {
  name: 'BaiduMap',
  mounted() {
    this.initMap();
    if (typeof BMapGL !== 'undefined') {
      this.initMap();
    } else {
      console.error('百度地图 API 加载失败');
    }
  },

  methods: {
    initMap() {
      const mapContainer = this.$refs.baiduMap;
      const map = new BMapGL.Map(mapContainer); // 创建地图实例
      const point = new BMapGL.Point(116.404, 39.915); // 创建地图中心点坐标
      map.centerAndZoom(point, 15); // 设置地图的中心和缩放级别
      map.enableScrollWheelZoom(true); // 启用滚轮缩放

      const marker = new BMapGL.Marker(point); // 创建标记
      map.addOverlay(marker); // 在地图上添加标记

      // 标记点击事件
      marker.addEventListener("click", function () {
        alert("您点击了标注");
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.home {
  box-sizing: border-box;

  .topbarBox {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 3.875rem
      /* 62/16 */
    ;
    // background-color: #545c64;

  }

  .map-container {
    width: 100%;
    height: 56.25rem /* 900/16 */;
    
  }
}
</style>