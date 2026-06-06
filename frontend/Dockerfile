# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app

# 设置npm镜像
RUN npm config set registry https://registry.npmmirror.com

# 先复制package文件，利用缓存
COPY package.json ./
RUN npm install

# 复制源码并构建
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 复制构建产物
COPY --from=builder /app/dist .

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 3000

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
