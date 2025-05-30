<template>
  <div ref="chartContainer" :style="{ width: width, height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts/core';
import { 
  GaugeChart, 
  LineChart, 
  BarChart 
} from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册必要的组件
echarts.use([
  GaugeChart,
  LineChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DatasetComponent,
  CanvasRenderer
]);

const props = defineProps({
  type: {
    type: String,
    default: 'gauge',
    validator: (value) => ['gauge', 'line', 'bar'].includes(value)
  },
  title: String,
  data: Object,
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '400px'
  },
  color: {
    type: String,
    default: '#4da6ff'
  },
  unit: {
    type: String,
    default: '%'
  }
});

const chartContainer = ref(null);
let chartInstance = null;

// 根据类型生成图表选项
const generateOption = () => {
  switch(props.type) {
    case 'gauge':
      return {
        series: [{
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 10,
          radius: '90%',
          axisLine: {
            lineStyle: {
              width: 15,
              color: [
                [0.3, '#91cc75'],
                [0.7, '#ffdb5c'],
                [1, '#ff6b6b']
              ]
            }
          },
          axisLabel: {
            distance: -25,
            fontSize: 14,
            color: '#e0e6ff'
          },
          axisTick: {
            length: 12,
            lineStyle: {
              color: 'auto'
            }
          },
          splitLine: {
            length: 20,
            lineStyle: {
              color: 'auto'
            }
          },
          pointer: {
            icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
            length: '60%',
            width: 12,
            offsetCenter: [0, '-20%'],
            itemStyle: {
              color: 'auto'
            }
          },
          detail: {
            valueAnimation: true,
            fontSize: 40,
            offsetCenter: [0, '20%'],
            fontWeight: 'bold',
            formatter: `{value}${props.unit}`,
            color: '#e0e6ff'
          },
          data: [{
            value: props.data.value
          }]
        }]
      };
    case 'line':
      return {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: props.data.timeData,
          axisLine: {
            lineStyle: {
              color: '#5d6a9a'
            }
          },
          axisLabel: {
            color: '#8c9bdd'
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: props.data.max || 100,
          axisLabel: {
            formatter: `{value}${props.unit}`,
            color: '#8c9bdd'
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(92, 105, 154, 0.2)'
            }
          }
        },
        series: [{
          name: props.title,
          type: 'line',
          data: props.data.values,
          smooth: true,
          symbol: 'none',
          lineStyle: {
            width: 3,
            color: props.color
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: `${props.color}60` },
              { offset: 1, color: `${props.color}10` }
            ])
          }
        }]
      };
    default:
      return {};
  }
};

// 初始化图表
onMounted(() => {
  if (chartContainer.value) {
    chartInstance = echarts.init(chartContainer.value);
    chartInstance.setOption(generateOption());
    
    // 响应式调整
    window.addEventListener('resize', handleResize);
  }
});

// 数据变化时更新图表
watch(() => props.data, (newData) => {
  if (chartInstance && newData) {
    chartInstance.setOption(generateOption());
  }
}, { deep: true });

// 组件卸载时销毁实例
onBeforeUnmount(() => {
  if (chartInstance) {
    window.removeEventListener('resize', handleResize);
    chartInstance.dispose();
    chartInstance = null;
  }
});

// 响应窗口大小变化
function handleResize() {
  if (chartInstance) {
    chartInstance.resize();
  }
}
</script>