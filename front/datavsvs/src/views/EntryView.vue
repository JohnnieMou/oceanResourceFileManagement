<template>
    <div class="entry-container">
        <div class="background-image"></div>
        <div class="content">
            <p>海洋工程与能源学院是广东海洋大学特色鲜明的工科学院。前身是海洋工程学院，<span
                    class="highlight">2022年7月</span>，为更好凝练学科方向，整合资源优势，调出船舶与海洋工程专业，调入能源与动力工程、建筑环境与能源应用工程<span
                    class="highlight">2个本科专业</span>，海洋工程学院更名为海洋工程与能源学院。目前，下设党政办公室、学生工作组、<span
                    class="highlight">3个教学系（港口航道与海岸工程系、工程管理系、热能与动力工程系）</span>，及<span
                    class="highlight">1个实验教学机构</span>，目前有本科生<span class="highlight">1872</span>，硕士研究生<span
                    class="highlight">14人</span>。</p>
            <el-button class="enter-btn" type="primary" @click="enTryHomeView">进入</el-button>
        </div>
    </div>
</template>

<script>
export default {
    name: 'EntryView',
    methods: {
        enTryHomeView() {
            this.$router.push({
                path: '/home',
            });
            // console.log('进入首页');

        }
    }
}
</script>

<style scoped>
.entry-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
    width: 100vw;
    /* 使容器宽度与视口宽度相同 */
    overflow: hidden;
    /* 防止内容溢出 */
    position: relative;
    /* 设置相对定位 */
}

.background-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url('@/assets/fenmian2.webp');
    /* 使用绝对路径引入图片 */
    background-size: cover;
    /* 使图片覆盖整个容器 */
    background-position: center;
    /* 图片居中显示 */
    z-index: -1;
    /* 确保图片在内容下方 */
}

.content {
    text-align: center;
    margin: 0 10%;
    /* 与两边留有间隙 */
    padding: 20px;
    background-color: rgba(0, 123, 255, 0.298);
    /* 半透明背景，增强文字可读性 */
    border-radius: 10px;
    z-index: 1;
    /* 确保内容在图片上方 */
}

.highlight {
    color: #ffd700;
    /* 显眼的黄色 */
    font-size: 1.2em;
    font-weight: bold;
}

.enter-btn {
    padding: 10px 20px;
    background-color: #0056b3;
    /* 深蓝色按钮 */
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1em;
    transition: background-color 0.3s ease;
}

.enter-btn:hover {
    background-color: #004080;
}
</style>