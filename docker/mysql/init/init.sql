CREATE DATABASE  IF NOT EXISTS `ecommerce` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ecommerce`;
-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: localhost    Database: ecommerce
-- ------------------------------------------------------
-- Server version	8.0.28-0ubuntu4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `district` varchar(50) DEFAULT NULL,
  `full_name` varchar(50) DEFAULT NULL,
  `note` varchar(100) DEFAULT NULL,
  `phone_number` varchar(15) DEFAULT NULL,
  `province` varchar(100) DEFAULT NULL,
  `street` varchar(50) DEFAULT NULL,
  `ward` varchar(50) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKda8tuywtf0gb6sedwk7la1pgi` (`user_id`),
  CONSTRAINT `FKda8tuywtf0gb6sedwk7la1pgi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,'Thành phố Bắc Giang','Sang ne ','','0379641598','Tỉnh Bắc Giang','0x570d81A459Ad17B9B297Fc1592Efda6B55A715e3','Phường Tân Tiến',8),(2,'Huyện Châu Thành','Sang ne ','','+1 6562294949','Tỉnh Tiền Giang','0x570d81A459Ad17B9B297Fc1592Efda6B55A715e3','Xã Kim Sơn',8),(3,'Thị xã Nghĩa Lộ','Tan Sang','','0379641598','Tỉnh Yên Bái','0x570d81A459Ad17B9B297Fc1592Efda6B55A715e3','Xã Thanh Lương',8),(4,'Huyện Phú Lương','Sang ne ','','0379641598','Tỉnh Thái Nguyên','0x570d81A459Ad17B9B297Fc1592Efda6B55A715e3','Xã Tức Tranh',8);
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount` int DEFAULT NULL,
  `original_price` int DEFAULT NULL,
  `total_amount` decimal(19,2) DEFAULT NULL,
  `total_discounted_price` int DEFAULT NULL,
  `total_items` int DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9emlp6m95v5er2bcqkjsw48he` (`user_id`),
  CONSTRAINT `FKl70asp4l4w0jmbm1tqyofho4o` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (1,0,0,NULL,0,0,8),(2,0,0,NULL,0,0,1),(4,0,0,NULL,0,0,4);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount_percent` int DEFAULT NULL,
  `discounted_price` int DEFAULT NULL,
  `price` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `size` varchar(255) DEFAULT NULL,
  `cart_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1uobyhgl1wvgt1jpccia8xxs3` (`cart_id`),
  KEY `FKjcyd5wv4igqnw413rgxbfu4nv` (`product_id`),
  CONSTRAINT `FK1uobyhgl1wvgt1jpccia8xxs3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`),
  CONSTRAINT `FKjcyd5wv4igqnw413rgxbfu4nv` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_parent` bit(1) NOT NULL,
  `level` int NOT NULL,
  `name` varchar(50) NOT NULL,
  `parent_category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_46ccwnsi9409t36lurvtyljak` (`name`),
  KEY `FKs2ride9gvilxy2tcuv7witnxc` (`parent_category_id`),
  CONSTRAINT `FKs2ride9gvilxy2tcuv7witnxc` FOREIGN KEY (`parent_category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (7,_binary '',1,'Phone',NULL),(8,_binary '\0',2,'Apple',7),(9,_binary '\0',2,'Xiaomi',7),(10,_binary '\0',2,'OPPO',7),(11,_binary '',1,'Laptop',NULL),(12,_binary '\0',2,'Acer',11),(13,_binary '\0',2,'ASUS',11),(14,_binary '\0',2,'Dell',11),(15,_binary '\0',2,'HP',11),(16,_binary '',1,'Accessory',NULL),(17,_binary '\0',2,'Anker',16),(18,_binary '\0',2,'Sony',16),(19,_binary '',1,'Tablet',NULL),(20,_binary '\0',2,'Samsung',19),(21,_binary '\0',2,'OnePlus',7),(22,_binary '\0',2,'2-in-1',11),(23,_binary '',1,'accessories',NULL),(24,_binary '\0',2,'Monitor',23),(25,_binary '\0',2,'Mouse',23),(26,_binary '',1,'desktop-computers',NULL),(27,_binary '\0',2,'Gaming',11),(28,_binary '\0',2,'Keyboard',23),(29,_binary '',1,'other-products',NULL),(30,_binary '\0',2,'Gaming Console',29);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `image`
--

DROP TABLE IF EXISTS `image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `download_url` varchar(500) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `product` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKehbjjsruqumpjyftghvihq383` (`product`),
  CONSTRAINT `FKehbjjsruqumpjyftghvihq383` FOREIGN KEY (`product`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `image`
--

LOCK TABLES `image` WRITE;
/*!40000 ALTER TABLE `image` DISABLE KEYS */;
INSERT INTO `image` VALUES (1,'https://cafefcdn.com/203337114487263232/2024/9/20/l1050353-16956275916131814288024-1695636379427-1695636379591231233432-17268244971181023798223-1726836228880-17268362294161090248665.jpeg','Product 1 Image 1','JPG',1),(2,'https://cdni.dienthoaivui.com.vn/x,webp,q100/https://dashboard.dienthoaivui.com.vn/uploads/wp-content/uploads/images/iphone-15-pro-max-256gb-titan-tu-nhien-cu-tray-xuoc-1.jpg','Product 1 Image 2','JPG',1),(3,'https://imgproxy7.tinhte.vn/e7iz_lClja1y_ttVtInCOv0HE7j7N4TcWvs64lmYQu0/w:500/plain/https://photo2.tinhte.vn/data/attachment-files/2023/09/8127395_tren-tay-iphone-15-pro-ma-natural-titanium-tinhte.12.jpg','Product 1 Image 3','JPG',1),(4,'https://shopdunk.com/images/uploaded/iphone-15-xam-titan/dap-hop-iphone-15-titan-tu-nhien.jpg','Product 1 Image 4','JPG',1),(5,'https://mobileworld.com.vn/uploads/news/01_2023/pro7.jpg','Product 2 Image 1','JPG',2),(6,'https://synnexfpt.com/wp-content/uploads/2023/05/xiaomi-13-pro-trang-1.jpg','Product 2 Image 2','JPG',2),(7,'https://didongthongminh.vn/upload_images/images/products/xiaomi-mi/original/xiaomi-13-pro-2_1685956373_1_copy.jpg','Product 2 Image 3','JPG',2),(8,'https://cdn2.cellphones.com.vn/insecure/rs:fill:0:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/1/3/13_prooo_4_.jpg','Product 2 Image 4','JPG',2),(9,'https://cdn2.fptshop.com.vn/unsafe/1920x0/filters:format(webp):quality(75)/2022_2_24_637813290369252329_oppo-find-x5-pro-ra-mat-cover.jpg','Product 3 Image 1','JPG',3),(10,'https://m.media-amazon.com/images/I/71udb5UdYxL._AC_SL1412_.jpg','Product 3 Image 2','JPG',3),(11,'https://static.fnac-static.com/multimedia/Images/FR/NR/44/c7/d8/14206788/1541-4/tsp20220301100513/OPPO-FIND-X5-PRO-256GB-BLACK.jpg','Product 3 Image 3','JPG',3),(12,'https://images.techadvisor.com/cmsdata/reviews/3813726/oppo_find_x5_pro-06_thumb.jpg','Product 3 Image 4','JPG',3),(13,'https://no1computer.vn/images/products/2023/02/10/large/acer-nitro-5-rtx-3060-jpg_1675967112-copy-1.jpg','Product 4 Image 1','JPG',4),(14,'https://laptop88.vn/media/lib/08-03-2023/acer-nitro-5-5669.jpg','Product 4 Image 2','JPG',4),(15,'https://laptop3mien.vn/wp-content/uploads/2023/06/Nitro-5-Eagle-AN515-57-54MV-3.jpg','Product 4 Image 3','JPG',4),(16,'https://cdn-v2.didongviet.vn/files/media/catalog/product/l/a/laptop-gaming-acer-nitro-5-eagle-an515-57-5669-didongviet.jpg','Product 4 Image 4','JPG',4),(17,'https://laptop88.vn/media/product/7543_528379232.jpg','Product 5 Image 1','JPG',5),(18,'https://cdn.tgdd.vn/Products/Images/44/305745/asus-gaming-rog-strix-scar-17-g733pz-r9-ll980w-thumb-600x600.jpg','Product 5 Image 2','JPG',5),(19,'https://bizweb.dktcdn.net/100/497/222/products/20200824120842b374cd8323a5401d.jpg?v=1697689086317','Product 5 Image 3','JPG',5),(20,'https://cdn.tgdd.vn/Files/2022/07/29/1451311/asus-ra-mat-laptop-gaming-rog-strix-scar-17-se-cau-hinh-sieu-khung-5.jpg','Product 5 Image 4','JPG',5),(21,'https://bizweb.dktcdn.net/thumb/grande/100/512/769/products/dell-xps-13-9340-2.webp?v=1716793972937','Product 6 Image 1','JPG',6),(22,'https://phucngoc.vn/Data/images/laptop-dell-xps-13-plus-9320.jpg','Product 6 Image 2','JPG',6),(23,'https://product.hstatic.net/1000089469/product/dell-xps-l322x-core-i5_master.jpg','Product 6 Image 3','JPG',6),(24,'https://file.hstatic.net/200000484561/file/12_6eea69fae5754fc6a3a14d62f7ff938c_grande.png','Product 6 Image 4','PNG',6),(25,'https://lapvip.vn/upload/products/original/hp-spectre-x360-14-2024-nightfall-black-1705650060.jpg','Product 7 Image 1','JPG',7),(26,'https://mac24h.vn/images/detailed/94/spectre-x360-2-in-1-14-2024-mac24h.jpg','Product 7 Image 2','JPG',7),(27,'https://www.cnet.com/a/img/resize/eae3840288fe99145fa33596146ecb448eff6a1b/hub/2021/04/12/ec06344f-4113-4966-9b4f-8fdcd129869f/014-hp-spectre-x360-14.jpg?auto=webp&fit=crop&height=900&width=1200','Product 7 Image 3','JPG',7),(28,'https://dangvu.vn/wp-content/uploads/2023/09/Surface-Laptop-5-va-HP-Spectre-X360-14-2023-Nen-mua-may-nao-15.webp','Product 7 Image 4','JPG',7),(29,'https://phukienvang.com/wp-content/uploads/2023/11/sac-du-phong-anker-powercore-iii-elite-25600-pd-60w-a1290-3.webp','Product 8 Image 1','webp',8),(30,'https://hanoicomputercdn.com/media/lib/14-09-2023/pin-sac-du-phong-anker-powercore-iii-elite-26k-a1290-pd-60w-25600mah-mau-den-5.jpg','Product 8 Image 2','jpg',8),(31,'https://phukienvang.com/wp-content/uploads/2023/11/sac-du-phong-anker-powercore-iii-elite-25600-pd-60w-a1290-4.webp','Product 8 Image 3','webp',8),(32,'https://cdni.dienthoaivui.com.vn/x,webp,q100/https://dashboard.dienthoaivui.com.vn/uploads/wp-content/uploads/2020/03/pin-du-phong-anker-powercore-elite-20000mah-a1273.jpg','Product 8 Image 4','jpg',8),(33,'https://minhvietstore.vn/wp-content/uploads/2021/03/xiaomiviet.vn-pin-sac-du-phong-xiaomi-power-bank-3-gen-3-30-000mah-xiaomi-power-bank-3--30.000-mah.jpg.webp','Product 9 Image 1','webp',9),(34,'https://gntek.vn/wp-content/uploads/2023/08/image-2023-08-04T212416.591.png.webp','Product 9 Image 2','webp',9),(35,'https://cdn.dienmaygiakhanh.com/Products/Images/57/216136/pin-sac-du-phong-polymer-10000mah-xiaomi-mi-18w-avatar-1-600x600.jpg','Product 9 Image 3','jpg',9),(36,'https://mivietnam.vn/wp-content/uploads/2019/12/MIVIETNAM-PINSDPPLM13ZM-01.jpg','Product 9 Image 4','jpg',9),(37,'https://baochauelec.com/upload/original-image/tai-nghe-sony-wh-1000xm5.png','Product 10 Image 1','png',10),(38,'https://antien.vn/files/products/photos/2024/04/13/tai-nghe-sony-wh1000xm5-blue-3.png','Product 10 Image 2','png',10),(39,'https://www.winwinstore.vn/wp-content/uploads/2022/05/sony-wh-1000xm5-box4.jpg','Product 10 Image 3','jpg',10),(40,'https://bizweb.dktcdn.net/thumb/1024x1024/100/479/913/products/tai-nghe-sony-wh1000xm5-2.jpg?v=1681816602693','Product 10 Image 4','jpg',10),(41,'https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/a/p/apple-airpods-4-chong-on-chu-dong-thumb_1.png','Product 11 Image 1','png',11),(42,'https://tinphatapple.vn/wp-content/uploads/2023/06/2b9d14ac2ef74a72a58cd787521739e8.jpeg','Product 11 Image 2','jpeg',11),(43,'https://tuanphong.vn/pictures/full/2020/03/1584963297-349-tai-nghe-bluetooth-apple-airpods-pro-7.jpg','Product 11 Image 3','jpg',11),(44,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRp71ooFxMgt6QW3MmhvW8BxxqCT0n1xSACQQ&s','Product 12 Image 1','jpg',12),(45,'https://cdn.24h.com.vn/upload/1-2022/images/2022-02-18/img_0273-1645199360-329-width740height493.jpg','Product 12 Image 2','jpg',12),(46,'https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/a/p/apple-airpods-4-chong-on-chu-dong-thumb_1.png ','Product 13 Image 1','png',13),(47,'https://tinphatapple.vn/wp-content/uploads/2023/06/2b9d14ac2ef74a72a58cd787521739e8.jpeg','Product 13 Image 2','jpeg',13),(48,'https://tuanphong.vn/pictures/full/2020/03/1584963297-349-tai-nghe-bluetooth-apple-airpods-pro-7.jpg','Product 13 Image 3','jpg',13),(49,'https://shopdunk.com/images/thumbs/0005889_macbook-air-m2-mau-midnight_1600.jpeg ','Product 14 Image 1','jpeg',14),(50,'https://bizweb.dktcdn.net/thumb/1024x1024/100/318/659/products/mba15-midnight-1-4-1024x787-bed8b970-41ef-4333-bc23-452185704cea.jpg?v=1687419564867 ','Product 14 Image 2','jpg',14),(51,'https://shopdunk.com/images/uploaded/macbook-midnight.jpg ','Product 14 Image 3','jpg',14),(52,'https://i.ebayimg.com/images/g/X8MAAOSwdndmM67A/s-l400.jpg ','Product 15 Image 1','jpg',15),(53,'https://media.croma.com/image/upload/v1740666346/Croma%20Assets/Communication/Mobiles/Images/304450_1_ostrdb.png ','Product 15 Image 2','png',15),(54,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRq9lQ4ciwDs8TiFLNIntGdkI2JDwwc-oDNFg&s ','Product 15 Image 3','jpg',15),(55,'https://cdn-dynmedia-1.microsoft.com/is/image/microsoftcorp/Highlight-Consumer-Laptop-Studio-2-Platinum-001:VP1-539x440 ','Product 16 Image 1','jpg',16),(56,'https://surfacecity.vn/wp-content/uploads/surface-laptop-studio-2-nvidia-rtx-4060-3.png ','Product 16 Image 2','png',16),(57,'https://dangvu.vn/wp-content/uploads/2024/05/Laptop-Studio-2.2.webp ','Product 16 Image 3','jpg',16),(58,'https://phongvukinhbac.vn/wp-content/uploads/2021/09/man-hinh-lg-utragear-gaming-34gn850-b-phong-vu-kinh-bac-min.jpg10-min.jpg ','Product 17 Image 1','jpg',17),(59,'https://m.media-amazon.com/images/I/612XqUdi6KL._AC_UF1000,1000_QL80_.jpg ','Product 17 Image 2','jpg',17),(60,'https://i.rtings.com/assets/products/7A4bsXUk/lg-34gn850-b/design-small.jpg?format=auto ','Product 17 Image 3','jpg',17),(61,'https://d28jzcg6y4v9j1.cloudfront.net/chuot_logitech_mx_3s_co_thiet_ke_chuan_cong_thai_hoc_1719808317791.jpg ','Product 18 Image 1','jpg',18),(62,'https://cdn2.cellphones.com.vn/200x/media/catalog/product/s/f/sfeftet466554.jpg ','Product 18 Image 2','jpg',18),(63,'https://nguyencongpc.vn/media/product/17312-logitech-mx-master-3-co-mau-18_1_11zon.jpg ','Product 18 Image 3','jpg',18),(64,'https://www.apple.com/newsroom/images/live-action/wwdc-2023/standard/mac-studio-mac-pro/Apple-WWDC23-Mac-Pro-M2-Ultra-Mac-Studio-M2-Max-M2-Ultra-230605.jpg.news_app_ed.jpg ','Product 19 Image 1','jpg',19),(65,'https://macstores.vn/wp-content/uploads/2023/06/studio_display_1.jpg ','Product 19 Image 2','jpg',19),(66,'https://www.didongmy.com/vnt_upload/news/06_2023/apple-mac-studio-m2-ra-mat-didongmy.jpg ','Product 19 Image 3','jpg',19),(67,'https://laptoplc.com.vn/wp-content/uploads/2023/07/11-1-1024x404.jpeg ','Product 20 Image 1','jpeg',20),(68,'https://bizweb.dktcdn.net/thumb/grande/100/512/769/products/dsc2782-copy-0ddd4659-508a-4a83-af5f-25f640d964da.jpg?v=1718611392320 ','Product 20 Image 2','JPG',20),(69,'https://laptopaz.vn/media/product/2196_2.jpg ','Product 20 Image 3','JPG',20),(70,'https://product.hstatic.net/200000837185/product/1_56e0a7a74b95400bb281ad0df3c9d9f2.png ','Product 21 Image 1','png',21),(71,'https://azaudio.vn/wp-content/uploads/2024/12/keychron-q1-max-qmk-via-wireless-side-printed-keycaps-carbon-black-1.jpg ','Product 21 Image 2','jpg',21),(72,'https://bizweb.dktcdn.net/thumb/1024x1024/100/329/122/products/ban-phim-co-khong-day-keychron-q1-he-carbon-black-rgb-knob-hotswap-02.jpg?v=1717649544980 ','Product 21 Image 3','JPG',21),(73,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRBMID2z7T97Y3dVtKvIM1IUaMd5BbwUu3ZHg&s ','Product 22 Image 1','JPG',22),(74,'https://herogame.vn/ad-min/assets/js/libs/kcfinder/upload_img2/images/Vinh/Oct/may-ps5-digital-pro-new-model-2024-playstation-5-xach-tay-1.jpg ','Product 22 Image 2','jpg',22),(75,'https://product.hstatic.net/200000038580/product/sony-playstation-5-pro-announcement_15ec0c4519b043e6ba1fb5d36a979195.jpg ','Product 22 Image 3','jpg',22);
/*!40000 ALTER TABLE `image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `delivery_date` datetime(6) DEFAULT NULL,
  `discount_percent` int DEFAULT NULL,
  `discounted_price` int DEFAULT NULL,
  `price` int DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt4dc2r9nbvbujrljv3e23iibt` (`order_id`),
  KEY `FK551losx9j75ss5d6bfsqvijna` (`product_id`),
  CONSTRAINT `FK551losx9j75ss5d6bfsqvijna` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKt4dc2r9nbvbujrljv3e23iibt` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,'2025-06-10 12:38:57.430716',29,496290,699000,3,'10000mAh',1,9),(2,'2025-06-10 12:40:59.408498',11,2483100,2790000,1,'Standard',2,18),(3,'2025-06-10 12:50:23.860746',8,3495080,3799000,1,'i7/16GB/1TB SSD',3,7),(4,'2025-06-10 12:54:44.048275',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',4,4),(5,'2025-06-10 13:07:49.758305',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',5,4),(6,'2025-06-10 13:08:09.232315',9,30020900,32990000,1,'i7/16GB/1TB SSD',6,6),(7,'2025-06-10 13:25:43.414681',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',7,4),(8,'2025-06-10 13:30:03.519053',8,3495080,3799000,2,'i7/16GB/1TB SSD',8,7),(9,'2025-06-10 13:39:46.019526',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',9,4),(10,'2025-06-10 13:40:42.259844',17,7461700,8990000,1,'Một cỡ',10,10),(11,'2025-06-10 14:32:52.386746',14,18051400,20990000,1,'256GB',11,3),(12,'2025-06-10 14:33:42.364551',10,4491000,4990000,1,'ISO',12,21),(13,'2025-06-10 14:35:02.545639',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',13,4),(14,'2025-06-10 14:46:31.764797',14,60191400,69990000,1,'Ryzen 7/16GB/1TB SSD/RTX 4080',14,5),(15,'2025-06-10 15:26:46.900504',14,60191400,69990000,1,'Ryzen 7/16GB/1TB SSD/RTX 4080',15,5),(16,'2025-06-10 16:12:55.605116',14,60191400,69990000,2,'Ryzen 9/32GB/2TB SSD/RTX 4090',16,5),(17,'2025-06-10 16:12:55.608503',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',17,4),(18,'2025-06-11 01:05:23.267578',14,60191400,69990000,3,'Ryzen 7/16GB/1TB SSD/RTX 4080',18,5),(19,'2025-06-11 01:07:15.009255',17,7461700,8990000,1,'Một cỡ',19,10),(20,'2025-06-11 01:09:20.713368',14,18911400,21990000,1,'i7/16GB/1TB SSD/RTX 3050Ti',20,4),(21,'2025-06-11 02:02:06.771290',13,20001300,22990000,1,'256GB',21,2),(22,'2025-06-11 08:15:36.777392',19,1295190,1599000,1,'19200mAh',22,8),(23,'2025-06-11 08:40:44.422917',29,496290,699000,1,'20000mAh',23,9);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `delivery_date` datetime(6) DEFAULT NULL,
  `discount` int DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `order_status` enum('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') DEFAULT NULL,
  `original_price` int DEFAULT NULL,
  `payment_method` enum('VNPAY','COD') DEFAULT 'COD',
  `payment_status` enum('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED') DEFAULT NULL,
  `seller_id` bigint DEFAULT NULL,
  `total_discounted_price` int DEFAULT NULL,
  `total_items` int DEFAULT NULL,
  `order_address` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9nfnsqjw05qf2mk5r3jnirngs` (`order_address`),
  KEY `FKel9kyl84ego2otj2accfd8mr7` (`user_id`),
  CONSTRAINT `FK9nfnsqjw05qf2mk5r3jnirngs` FOREIGN KEY (`order_address`) REFERENCES `address` (`id`),
  CONSTRAINT `FKel9kyl84ego2otj2accfd8mr7` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,NULL,608130,'2025-06-03 12:38:57.426821','CANCELLED',2097000,'VNPAY','CANCELLED',3,1488870,3,1,8),(2,NULL,306900,'2025-06-03 12:40:59.401906','CONFIRMED',2790000,'VNPAY','PENDING',4,2483100,1,1,8),(3,NULL,303920,'2025-06-03 12:50:23.859080','PENDING',3799000,'COD','PENDING',5,3495080,1,1,8),(4,NULL,3078600,'2025-06-03 12:54:44.045739','PENDING',21990000,'COD','PENDING',6,18911400,1,1,8),(5,NULL,3078600,'2025-06-03 13:07:49.756232','CANCELLED',21990000,'VNPAY','REFUNDED',6,18911400,1,1,8),(6,NULL,2969100,'2025-06-03 13:08:09.229667','PENDING',32990000,'VNPAY','COMPLETED',4,30020900,1,1,8),(7,NULL,3078600,'2025-06-03 13:25:43.413369','PENDING',21990000,'COD','PENDING',6,18911400,1,1,8),(8,NULL,607840,'2025-06-03 13:30:03.516196','PENDING',7598000,'COD','PENDING',5,6990160,2,1,8),(9,NULL,3078600,'2025-06-03 13:39:46.018112','PENDING',21990000,'COD','PENDING',6,18911400,1,1,8),(10,NULL,1528300,'2025-06-03 13:40:42.258430','PENDING',8990000,'VNPAY','COMPLETED',4,7461700,1,1,8),(11,NULL,2938600,'2025-06-03 14:32:52.380403','PENDING',20990000,'VNPAY','PENDING',5,18051400,1,1,8),(12,NULL,499000,'2025-06-03 14:33:42.362239','PENDING',4990000,'VNPAY','PENDING',3,4491000,1,1,8),(13,NULL,3078600,'2025-06-03 14:35:02.542877','PENDING',21990000,'VNPAY','PENDING',6,18911400,1,1,8),(14,NULL,9798600,'2025-06-03 14:46:31.762749','PENDING',69990000,'VNPAY','PENDING',3,60191400,1,1,8),(15,NULL,9798600,'2025-06-03 15:26:46.891677','PENDING',69990000,'COD','PENDING',3,60191400,1,1,8),(16,NULL,19597200,'2025-06-03 16:12:55.603330','PENDING',139980000,'COD','PENDING',3,120382800,2,1,8),(17,NULL,3078600,'2025-06-03 16:12:55.607247','PENDING',21990000,'COD','PENDING',6,18911400,1,1,8),(18,NULL,29395800,'2025-06-04 01:05:23.253174','PENDING',209970000,'COD','PENDING',3,180574200,3,2,8),(19,NULL,1528300,'2025-06-04 01:07:15.007279','PENDING',8990000,'COD','PENDING',4,7461700,1,1,8),(20,'2025-06-04 01:11:42.347778',3078600,'2025-06-04 01:09:20.711773','DELIVERED',21990000,'COD','COMPLETED',6,18911400,1,1,8),(21,NULL,2988700,'2025-06-04 02:02:06.767594','PENDING',22990000,'VNPAY','PENDING',4,20001300,1,1,8),(22,NULL,303810,'2025-06-04 08:15:36.768772','PENDING',1599000,'VNPAY','COMPLETED',6,1295190,1,1,8),(23,NULL,202710,'2025-06-04 08:40:44.415654','PENDING',699000,'COD','PENDING',3,496290,1,1,8);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_details`
--

DROP TABLE IF EXISTS `payment_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `payment_date` datetime(6) DEFAULT NULL,
  `payment_log` text,
  `payment_method` enum('VNPAY','COD') DEFAULT NULL,
  `payment_status` enum('PENDING','COMPLETED','FAILED','CANCELLED','REFUNDED') DEFAULT NULL,
  `total_amount` int DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `vnp_response_code` varchar(255) DEFAULT NULL,
  `vnp_secure_hash` varchar(255) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_of2hvjrt3h42uja5aibie81tp` (`order_id`),
  CONSTRAINT `FK34yjcjptgtt05syk6x0t8s35b` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_details`
--

LOCK TABLES `payment_details` WRITE;
/*!40000 ALTER TABLE `payment_details` DISABLE KEYS */;
INSERT INTO `payment_details` VALUES (1,'2025-06-03 12:39:29.974150',NULL,NULL,'VNPAY','PENDING',1488870,'1_62906712','2025-06-03 12:39:29.974153',NULL,'3d4a7c1f137fe2485467cf4dedcc83065621e031ec93b76348cc657c620357608c86b1a528d45368b0a6654bed3d4aa8ce583b27f433afc7e3f2991b4cc1cef9',1),(13,'2025-06-03 15:18:39.152960','2025-06-03 15:21:45.817229','{\"vnp_Amount\":\"1891140000\",\"vnp_BankCode\":\"NCB\",\"vnp_BankTranNo\":\"VNP14996854\",\"vnp_CardType\":\"ATM\",\"vnp_OrderInfo\":\"Thanh toan don hang #5\",\"vnp_PayDate\":\"20250603151939\",\"vnp_ResponseCode\":\"00\",\"vnp_TmnCode\":\"N7MGBPT4\",\"vnp_TransactionNo\":\"14996854\",\"vnp_TransactionStatus\":\"00\",\"vnp_TxnRef\":\"5_96814198\",\"vnp_SecureHash\":\"668e5485ced7ef18ca10d20379f20c29baa6661fecaac2af409e0e4a20e0de5c3f5d1b262fd9dacaf158506a6ae004d8aea7cac2722f64d18029b601af4dd1fd\"}','VNPAY','COMPLETED',18911400,'5_96814198','2025-06-03 15:21:45.817919','00','7b095a5ec094f4daa903453eeb7df9bfbeab37b9e6d896aa5a28f57f3b46c8822d2391501a4cc8439940afbba13b64b6b4a51335ef438d980de82b1dc110899f',5),(14,'2025-06-03 15:21:50.350584','2025-06-03 15:22:27.739967','{\"step\":\"4\",\"vnp_Amount\":\"3002090000\",\"vnp_BankCode\":\"NCB\",\"vnp_BankTranNo\":\"VNP14996867\",\"vnp_CardType\":\"ATM\",\"vnp_OrderInfo\":\"Thanh toan don hang #6\",\"vnp_PayDate\":\"20250603152255\",\"vnp_ResponseCode\":\"00\",\"vnp_TmnCode\":\"N7MGBPT4\",\"vnp_TransactionNo\":\"14996867\",\"vnp_TransactionStatus\":\"00\",\"vnp_TxnRef\":\"6_94606853\",\"vnp_SecureHash\":\"ae5e9aa9e42c17ce61a0e47bd653cc55b4edf95a20e195f9cfd81896eacced931f755007a10278fcd9d61cf4a207e6c90ca82d643de671123e588535bbec8759\"}','VNPAY','COMPLETED',30020900,'6_94606853','2025-06-03 15:22:27.740792','00','6d22d3b3351e1a35942506cff0dd40f4d9dd3b7ca6c62923f46969d66c591d9012c579e49ea4ad9ed31e5883613281c2128981785f7c5e042479991e3f8280b0',6),(15,'2025-06-04 02:02:06.828998',NULL,NULL,'VNPAY','PENDING',20001300,'21_01302998','2025-06-04 02:02:06.829018',NULL,'3f8075a92a215a3f2f3b5c304582e087420db58c021215f4c6ca33fb734994e361f736282823a9ecf4264f215670ed916e4be0fe8ba82f31c10419a9ebe9e021',21),(16,'2025-06-04 08:15:36.866384','2025-06-04 08:16:02.389191','{\"step\":\"4\",\"vnp_Amount\":\"129519000\",\"vnp_BankCode\":\"NCB\",\"vnp_BankTranNo\":\"VNP14998145\",\"vnp_CardType\":\"ATM\",\"vnp_OrderInfo\":\"Thanh toan don hang #22\",\"vnp_PayDate\":\"20250604081630\",\"vnp_ResponseCode\":\"00\",\"vnp_TmnCode\":\"N7MGBPT4\",\"vnp_TransactionNo\":\"14998145\",\"vnp_TransactionStatus\":\"00\",\"vnp_TxnRef\":\"22_29308523\",\"vnp_SecureHash\":\"fb897b2c0ae24f27c004fde96728ec2025d50f6868fd46e0bb6a7c4431de48bcaa0c2667b3e9cf6b67fa3d8e4dd5497a011b08cf05572639aff4404721b21082\"}','VNPAY','COMPLETED',1295190,'22_29308523','2025-06-04 08:16:02.391145','00','9c6c8c1a6150bc40dfbf91d084b95f07db1a6bf1746d04a81f6bce94d88dba303eeec71acc03ae333933261d1d7f9a23c936b6f4543598da7a368f8f77f1a0de',22);
/*!40000 ALTER TABLE `payment_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `average_rating` double NOT NULL DEFAULT '0',
  `battery_capacity` varchar(255) DEFAULT NULL,
  `battery_type` varchar(255) DEFAULT NULL,
  `brand` varchar(50) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `connection_port` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `detailed_review` text,
  `dimension` varchar(255) DEFAULT NULL,
  `discount_persent` int DEFAULT NULL,
  `discounted_price` int DEFAULT NULL,
  `num_ratings` int NOT NULL DEFAULT '0',
  `powerful_performance` text,
  `price` int NOT NULL,
  `quantity_sold` bigint DEFAULT NULL,
  `ram_capacity` varchar(255) DEFAULT NULL,
  `rom_capacity` varchar(255) DEFAULT NULL,
  `screen_size` varchar(255) DEFAULT NULL,
  `seller_id` bigint DEFAULT NULL,
  `title` varchar(100) DEFAULT NULL,
  `weight` varchar(255) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1mtsbur82frn64de7balymq9s` (`category_id`),
  CONSTRAINT `FK1mtsbur82frn64de7balymq9s` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,0,'4422 mAh','Li-Ion','Apple','Titan tự nhiên','Type-C','2025-06-03 12:37:39.955809','Điện thoại thông minh cao cấp nhất của Apple với hiệu năng vượt trội và camera tiên tiến.','iPhone 15 Pro Max là một chiếc smartphone toàn diện, hội tụ những công nghệ tiên tiến nhất của Apple. Đây là lựa chọn hàng đầu cho những ai tìm kiếm một thiết bị mạnh mẽ, camera xuất sắc và trải nghiệm người dùng cao cấp. Mặc dù giá thành không hề rẻ, nhưng những gì bạn nhận được hoàn toàn xứng đáng.','Dài 159.9 mm - Ngang 76.7 mm - Dày 8.25 mm',6,32890600,0,'Với chip A17 Pro, iPhone 15 Pro Max mang đến hiệu năng vượt trội, dễ dàng xử lý mọi tác vụ từ cơ bản đến chuyên sâu một cách mượt mà. Các ứng dụng nặng, game đồ họa cao đều chạy trơn tru. Điểm số benchmark ấn tượng, cho thấy sức mạnh xử lý hàng đầu trong thế giới smartphone. Đây thực sự là một cỗ máy hiệu suất ấn tượng.',34990000,NULL,'8 GB','256 GB','6.7 inch - Tần số quét 120 Hz',3,'iPhone 15 Pro Max','221 g',8),(2,0,'4820 mAh','Li-Po','Xiaomi','Trắng Ceramic','Type-C','2025-06-03 12:37:39.975001','Điện thoại Xiaomi cao cấp với camera Leica chuyên nghiệp.','Xiaomi 13 Pro là một chiếc điện thoại cao cấp với sự hợp tác danh tiếng Leica cho camera, mang lại chất lượng ảnh chụp chuyên nghiệp. Máy có thiết kế sang trọng, màn hình AMOLED rực rỡ và hiệu năng mạnh mẽ từ chip Snapdragon đầu bảng.','Dài 162.9 mm - Ngang 74.6 mm - Dày 8.4 mm',13,20001300,0,'Được trang bị chip Snapdragon 8 Gen 2, Xiaomi 13 Pro cung cấp hiệu năng ấn tượng, xử lý mượt mà mọi tác vụ từ giải trí đến công việc. Khả năng đa nhiệm tốt và chơi game đồ họa cao không gặp trở ngại.',22990000,1,'12 GB','256GB/512GB','6.73 inch - Tần số quét 120 Hz',4,'Xiaomi 13 Pro','210 g',9),(3,0,'5000 mAh','Li-Po','OPPO','Glaze Black','Type-C','2025-06-03 12:37:39.989678','Điện thoại OPPO flagship với thiết kế nguyên khối và sạc siêu nhanh.','OPPO Find X5 Pro nổi bật với thiết kế gốm cao cấp, chip xử lý hình ảnh MariSilicon X độc quyền và camera hợp tác cùng Hasselblad. Máy mang lại trải nghiệm nhiếp ảnh di động đỉnh cao, sạc siêu nhanh SuperVOOC và màn hình LTPO AMOLED tuyệt đẹp.','Dài 163.7 mm - Ngang 73.9 mm - Dày 8.5 mm',14,18051400,0,'Với chip Snapdragon 8 Gen 1 và NPU MariSilicon X, OPPO Find X5 Pro mang đến hiệu năng mạnh mẽ và khả năng xử lý hình ảnh vượt trội. Máy đáp ứng tốt các tác vụ nặng, chơi game mượt mà và tối ưu hóa cho việc chụp ảnh, quay video chất lượng cao.',20990000,1,'12 GB','256GB/512GB','6.7 inch - Tần số quét 120 Hz',5,'OPPO Find X5 Pro','218 g',10),(4,5,'57.5 Wh','Li-Ion','Acer','Đen','USB-A, USB-C (bao gồm Thunderbolt 4), HDMI, RJ45, Jack 3.5mm','2025-06-03 12:37:40.006075','Laptop gaming mạnh mẽ với chip Intel Core i5 và card đồ họa rời RTX 3050.','Acer Nitro 5 Eagle là một chiếc laptop gaming phổ thông mạnh mẽ, phù hợp cho game thủ với ngân sách vừa phải. Máy có thiết kế hầm hố, bàn phím RGB và hệ thống tản nhiệt hiệu quả, mang lại trải nghiệm chơi game ổn định.','Dài 363.4 mm - Rộng 255 mm - Dày 23.9 mm',14,18911400,1,'Với CPU Intel Core i5/i7 thế hệ 11 và card đồ họa NVIDIA GeForce RTX series 30, Nitro 5 Eagle cung cấp hiệu năng đủ để chiến các tựa game eSports và nhiều game AAA ở cài đặt phù hợp. Khả năng nâng cấp RAM và SSD cũng là một điểm cộng.',21990000,6,'8 GB/16 GB','512GB SSD/1TB SSD','15.6 inch - Tần số quét 144 Hz',6,'Laptop Acer Nitro 5 Eagle','Khoảng 2.2 kg',12),(5,0,'90 Wh','Li-Polymer','ASUS','Đen','USB-A, USB-C (DisplayPort, G-SYNC, Power Delivery), HDMI 2.1, LAN RJ45, Jack 3.5mm','2025-06-03 12:37:40.020304','Laptop gaming cao cấp với chip AMD Ryzen 9 và card đồ họa rời RTX 4090.','ASUS ROG Strix SCAR 17 là một cỗ máy gaming đỉnh cao, được thiết kế cho các game thủ chuyên nghiệp và người dùng yêu cầu hiệu suất tối đa. Máy sở hữu màn hình tần số quét siêu cao, hệ thống tản nhiệt tiên tiến và bàn phím cơ quang học mang lại trải nghiệm chơi game không giới hạn.','Dài 395 mm - Rộng 282 mm - Dày 23.4-28.3 mm',14,60191400,0,'Trang bị CPU AMD Ryzen 9 series mới nhất và GPU NVIDIA GeForce RTX 40 series hàng đầu, ROG Strix SCAR 17 mang đến sức mạnh xử lý đồ họa khủng khiếp, dễ dàng chinh phục mọi tựa game nặng nhất ở cài đặt cao nhất và các tác vụ sáng tạo chuyên nghiệp.',69990000,7,'16 GB/32 GB','1TB SSD/2TB SSD','17.3 inch - Tần số quét 240 Hz',3,'Laptop ASUS ROG Strix SCAR 17','Khoảng 3.0 kg',13),(6,0,'55 Wh','Li-Ion','Dell','Bạc','2 x Thunderbolt 4 (USB Type-C)','2025-06-03 12:37:40.034535','Laptop mỏng nhẹ cao cấp cho công việc và giải trí.','Dell XPS 13 Plus là biểu tượng của sự sang trọng và tính di động. Với thiết kế tối giản, viền màn hình siêu mỏng và bàn phím zero-lattice độc đáo, chiếc laptop này mang đến trải nghiệm làm việc và giải trí cao cấp, lý tưởng cho những người thường xuyên di chuyển.','Dài 295.3 mm - Rộng 199.04 mm - Dày 15.28 mm',9,30020900,0,'Với bộ vi xử lý Intel Core thế hệ mới nhất, Dell XPS 13 Plus đáp ứng mượt mà các tác vụ văn phòng, duyệt web, và chỉnh sửa ảnh nhẹ nhàng. Thiết kế tản nhiệt được cải tiến giúp duy trì hiệu suất ổn định trong một thân máy siêu mỏng.',32990000,1,'8 GB/16 GB','512GB SSD/1TB SSD','13.4 inch - Tần số quét 60 Hz',4,'Laptop Dell XPS 13','Khoảng 1.23 kg',14),(7,0,'68 Wh','Li-ion','HP','Xanh dương','1 x USB-A (HP Sleep and Charge), 2 x Thunderbolt 4 (USB-C với USB Power Delivery, DisplayPort™ 2.1, HP Sleep and Charge), 1 x HDMI 2.1, 1 x jack tai nghe/mic combo','2025-06-03 12:37:40.050429','Laptop lai 2 trong 1 với màn hình cảm ứng và thiết kế xoay gập linh hoạt.','HP Spectre x360 14 là một chiếc laptop 2-trong-1 cao cấp, kết hợp hoàn hảo giữa thiết kế thời thượng, tính linh hoạt và hiệu năng mạnh mẽ. Màn hình OLED rực rỡ, bút cảm ứng đi kèm và thời lượng pin tốt làm cho nó trở thành lựa chọn tuyệt vời cho cả công việc và sáng tạo.','Dài 313.7 mm - Rộng 220.4 mm - Dày 16.9 mm',8,3495080,0,'Được trang bị chip Intel Core Ultra mới nhất với NPU tích hợp, Spectre x360 14 mang lại hiệu suất ấn tượng và khả năng xử lý AI tiên tiến. Máy vận hành mượt mà các ứng dụng văn phòng, đồ họa và giải trí đa phương tiện, đồng thời tối ưu hóa năng lượng hiệu quả.',3799000,3,'8 GB/16 GB','512GB SSD/1TB SSD','14 inch - Tần số quét 48-120 Hz (OLED)',5,'Laptop HP Spectre x360 14','Khoảng 1.44 kg',15),(8,0,'25600mAh/19200mAh','Li-ion','Anker','Đen','2 x USB-A, 2 x USB-C (Đầu vào/ra USB-C PD)','2025-06-03 12:37:40.064332','Pin sạc dự phòng dung lượng lớn với nhiều cổng sạc nhanh.','Anker PowerCore III Elite là một pin sạc dự phòng dung lượng cực lớn, lý tưởng cho những chuyến đi dài hoặc những người dùng nhiều thiết bị. Với nhiều cổng sạc nhanh, nó có thể sạc đồng thời laptop, điện thoại và các phụ kiện khác một cách hiệu quả.','Dài 183.5 mm - Rộng 82.4 mm - Dày 24 mm',19,1295190,0,'Cung cấp công suất sạc mạnh mẽ qua cổng USB-C Power Delivery, Anker PowerCore III Elite có khả năng sạc nhanh cho cả laptop và các thiết bị di động. Công nghệ PowerIQ đảm bảo sạc tối ưu cho từng thiết bị.',1599000,1,NULL,NULL,NULL,6,'Sạc dự phòng Anker PowerCore III Elite','600 g',17),(9,0,'20000mAh/10000mAh','Li-Po','Xiaomi','Bạc','USB-A (output), USB-C (input/output), Micro-USB (input)','2025-06-03 12:37:40.077113','Pin sạc dự phòng dung lượng lớn, thiết kế mỏng nhẹ.','Xiaomi Mi Power Bank 3 là dòng pin sạc dự phòng phổ biến với thiết kế thanh lịch, nhiều tùy chọn dung lượng và giá cả phải chăng. Nó cung cấp khả năng sạc ổn định cho các thiết bị di động, là một phụ kiện hữu ích cho cuộc sống hàng ngày.','Tùy theo dung lượng, ví dụ bản 10000mAh: Dài 147.8 mm - Rộng 73.9 mm - Dày 15.3 mm',29,496290,0,'Hỗ trợ sạc nhanh hai chiều qua cổng USB-C (tùy model), Mi Power Bank 3 giúp nạp đầy năng lượng cho điện thoại và cả chính nó một cách nhanh chóng. Các cổng USB-A cho phép sạc đồng thời nhiều thiết bị.',699000,1,NULL,NULL,NULL,3,'Sạc dự phòng Xiaomi Mi Power Bank 3','Khoảng 250-450g tùy dung lượng',9),(10,0,'Lên đến 30 giờ (NC bật), 40 giờ (NC tắt)','Li-Ion (tích hợp)','Sony','Đen','USB Type-C (sạc), Jack 3.5mm','2025-06-03 12:37:40.089337','Tai nghe chống ồn hàng đầu với chất âm tuyệt đỉnh.','Sony WH-1000XM5 tiếp tục khẳng định vị thế dẫn đầu trong làng tai nghe chống ồn. Với thiết kế mới nhẹ nhàng hơn, chất âm cải thiện và khả năng chống ồn chủ động xuất sắc, đây là lựa chọn hoàn hảo cho những ai tìm kiếm sự yên tĩnh và trải nghiệm âm thanh đỉnh cao.',NULL,17,7461700,0,'Công nghệ chống ồn HD Processor QN1 và Integrated Processor V1 mang lại khả năng khử tiếng ồn vượt trội. Driver mới 30mm cho chất âm chi tiết, cân bằng. Thời lượng pin dài và sạc nhanh cũng là điểm mạnh của sản phẩm.',8990000,2,NULL,NULL,NULL,4,'Tai nghe Sony WH-1000XM5','Khoảng 250 g',18),(11,0,'Tai nghe: Đến 6 giờ nghe nhạc; Với hộp sạc: Đến 30 giờ nghe nhạc','Lithium-Ion','Apple','Trắng','Hộp sạc: USB-C','2025-06-03 12:37:40.100204','Tai nghe không dây cao cấp với khả năng chống ồn chủ động và âm thanh không gian.','AirPods Pro (Thế hệ 2) với cổng sạc USB-C mang đến trải nghiệm âm thanh không dây tiện lợi và chất lượng cao. Khả năng chống ồn chủ động hiệu quả, chế độ xuyên âm tự nhiên và âm thanh không gian mang lại trải nghiệm nghe đắm chìm.','Tai nghe: Dài 30.9 mm - Rộng 21.8 mm - Dày 24.0 mm; Hộp sạc: Cao 45.2 mm - Rộng 60.6 mm - Dày 21.7 mm',14,6011400,0,'Chip H2 mới cung cấp hiệu suất âm thanh vượt trội, cải thiện khả năng chống ồn và thời lượng pin. Các tính năng như Adaptive Audio và Personalized Spatial Audio nâng tầm trải nghiệm người dùng.',6990000,NULL,NULL,NULL,NULL,5,'Tai nghe AirPods Pro','Tai nghe: 5.3 g mỗi bên, Hộp sạc: 50.8 g',8),(12,0,'11200 mAh','Li-Po','Samsung','Graphite','USB Type-C','2025-06-03 12:37:40.113672','Máy tính bảng cao cấp với màn hình lớn và bút S Pen đi kèm.','Samsung Galaxy Tab S8 Ultra là một chiếc máy tính bảng Android khổng lồ, hướng đến người dùng chuyên nghiệp và sáng tạo. Màn hình Super AMOLED 14.6 inch tuyệt đẹp, bút S Pen cải tiến và hiệu năng mạnh mẽ từ chip Snapdragon đầu bảng mang lại trải nghiệm làm việc và giải trí ấn tượng.','Dài 326.4 mm - Rộng 208.6 mm - Dày 5.5 mm',11,24911100,0,'Với chip Snapdragon 8 Gen 1, Galaxy Tab S8 Ultra xử lý mượt mà các tác vụ nặng, đa nhiệm nhiều cửa sổ và các ứng dụng đồ họa. Bút S Pen độ trễ thấp và các tính năng DeX giúp biến nó thành một công cụ làm việc hiệu quả.',27990000,NULL,'8GB/12GB/16GB tùy phiên bản','128GB/256GB','14.6 inch - Tần số quét 120 Hz',6,'Máy tính bảng Samsung Galaxy Tab S8 Ultra','726 g (Wi-Fi)',20),(13,0,'40.88 Wh','Li-Po','Apple','Space Gray','Thunderbolt / USB 4 (Type-C)','2025-06-03 12:37:40.123483','Máy tính bảng mạnh mẽ với chip M2 và màn hình Liquid Retina XDR.','iPad Pro 12.9 inch với chip M2 là một cỗ máy mạnh mẽ dành cho các chuyên gia sáng tạo và người dùng đòi hỏi hiệu suất cao. Màn hình Liquid Retina XDR tuyệt đẹp, khả năng tương thích với Apple Pencil 2 và Magic Keyboard biến nó thành một công cụ đa năng cho công việc và giải trí.','Dài 280.6 mm - Ngang 214.9 mm - Dày 6.4 mm',10,27891000,0,'Chip M2 mang đến hiệu năng CPU và GPU vượt trội, cho phép iPad Pro xử lý mượt mà các ứng dụng đồ họa nặng, chỉnh sửa video 4K, và các tác vụ AR phức tạp. Khả năng đa nhiệm được cải thiện đáng kể với Stage Manager.',30990000,NULL,'8 GB','128GB/256GB/512GB','12.9 inch - Tần số quét 120 Hz (ProMotion)',3,'iPad Pro 12.9 inch (M2, 2022)','682 g',8),(14,0,'52.6 Wh','Li-Polymer','Apple','Midnight','2 x Thunderbolt / USB 4, Cổng sạc MagSafe 3, Jack tai nghe 3.5 mm','2025-06-03 12:37:40.135633','Laptop mỏng nhẹ với chip M2 mạnh mẽ, thời lượng pin dài và thiết kế không quạt.','MacBook Air M2 là sự kết hợp hoàn hảo giữa thiết kế mỏng nhẹ, hiệu năng mạnh mẽ và thời lượng pin ấn tượng. Với chip M2 mới, màn hình Liquid Retina đẹp mắt và thiết kế không quạt, đây là lựa chọn lý tưởng cho sinh viên, nhân viên văn phòng và những người thường xuyên di chuyển.','Dài 30.41 cm - Ngang 21.5 cm - Dày 1.13 cm',6,29130600,0,'Chip Apple M2 cung cấp hiệu suất CPU và GPU nhanh hơn đáng kể so với thế hệ trước, xử lý mượt mà các tác vụ hàng ngày, chỉnh sửa ảnh, video nhẹ và duyệt web. Thời lượng pin lên đến 18 giờ giúp bạn làm việc cả ngày mà không cần lo lắng.',30990000,NULL,'8GB/16GB','256GB/512GB/1TB','13.6 inch - Tần số quét 60 Hz',4,'MacBook Air M2','1.24 kg',8),(15,0,'5400 mAh','Li-Po','OnePlus','Flowy Emerald','Type-C','2025-06-03 12:37:40.148894','Flagship với chip Snapdragon 8 Gen 3, camera Hasselblad, sạc nhanh 100W và màn hình LTPO AMOLED 120Hz.','OnePlus 12 là một chiếc flagship toàn diện, mang đến hiệu năng siêu mạnh từ chip Snapdragon 8 Gen 3, camera Hasselblad thế hệ mới và sạc siêu nhanh SUPERVOOC. Màn hình LTPO AMOLED ProXDR tuyệt đẹp và thiết kế cao cấp hoàn thiện trải nghiệm người dùng.','Dài 164.3 mm - Ngang 75.8 mm - Dày 9.2 mm',9,20010900,0,'Với Snapdragon 8 Gen 3, OnePlus 12 dễ dàng cân mọi tác vụ nặng, từ gaming đồ họa cao đến xử lý video 8K. Hệ thống tản nhiệt kép Cryo-velocity đảm bảo hiệu suất ổn định và mát mẻ.',21990000,NULL,'12GB/16GB','256GB/512GB','6.82 inch - Tần số quét 120 Hz (LTPO AMOLED)',5,'OnePlus 12','220 g',21),(16,0,'Khoảng 56-58 Wh','Li-Ion','Microsoft','Platinum','2 x USB-C (Thunderbolt 4), 1 x USB-A 3.1, Khe đọc thẻ MicroSDXC, Jack tai nghe 3.5mm, Cổng Surface Connect','2025-06-03 12:37:40.163988','Laptop đa năng với màn hình có thể gập linh hoạt, bút Surface Slim Pen 2 và hiệu năng đồ họa mạnh mẽ.','Surface Laptop Studio 2 là một chiếc máy tính xách tay đa năng và mạnh mẽ, được thiết kế cho các chuyên gia sáng tạo. Với màn hình cảm ứng PixelSense Flow độc đáo có thể xoay gập linh hoạt, bút Surface Slim Pen 2 và hiệu năng đồ họa ấn tượng, nó đáp ứng mọi nhu cầu từ thiết kế, lập trình đến giải trí.','Dài 323 mm - Rộng 230 mm - Dày 22 mm',6,49810600,0,'Trang bị bộ vi xử lý Intel Core i7 thế hệ 13 và card đồ họa NVIDIA GeForce RTX series 40, Surface Laptop Studio 2 mang đến hiệu suất vượt trội cho các tác vụ đòi hỏi cao. Khả năng chuyển đổi giữa các chế độ sử dụng (laptop, stage, studio) tối ưu hóa cho từng công việc cụ thể.',52990000,NULL,'16GB/32GB','512GB SSD/1TB SSD','14.4 inch - Tần số quét 120 Hz',6,'Surface Laptop Studio 2','Khoảng 1.98 kg',22),(17,0,NULL,NULL,'LG','Black','2 x HDMI, 1 x DisplayPort 1.4, Hub USB 3.0 (1 upstream, 2 downstream), Jack tai nghe','2025-06-03 12:37:40.181183','Màn hình gaming cong UltraWide QHD với tần số quét 160Hz, HDR, sync G-Sync và FreeSync và độ phủ màu rộng.','LG UltraGear 34GN850-B là màn hình gaming cong UltraWide QHD mang đến trải nghiệm chơi game đắm chìm. Với tần số quét cao, thời gian phản hồi nhanh và hỗ trợ G-Sync/FreeSync, game thủ sẽ tận hưởng hình ảnh mượt mà, không xé hình.','Rộng 819.2 mm - Cao 464.1-574.1 mm (điều chỉnh) - Sâu 312.2 mm (có chân đế)',9,20010900,0,'Tấm nền Nano IPS cho màu sắc chính xác và góc nhìn rộng. Độ phân giải QHD sắc nét, tần số quét 160Hz và thời gian phản hồi 1ms (GtG) đảm bảo hiệu suất chơi game đỉnh cao. Hỗ trợ DisplayHDR 400 tăng cường độ tương phản.',21990000,NULL,NULL,NULL,'34 inch - Tần số quét 160 Hz',3,'UltraGear 34GN850-B','7.6 kg (có chân đế)',24),(18,0,'500 mAh (Lên đến 70 ngày)','Li-Po','Logitech','Graphite','USB-C (sạc), Bluetooth, Logi Bolt Receiver','2025-06-03 12:37:40.193779','Chuột không dây cao cấp với cảm biến 8K DPI, cuộn siêu nhanh và thiết kế công thái học.','Logitech MX Master 3S là chuột không dây cao cấp được thiết kế cho công việc chuyên nghiệp. Với cảm biến 8K DPI, cuộn MagSpeed siêu nhanh và yên tĩnh, cùng thiết kế công thái học, nó mang lại sự thoải mái và hiệu quả tối đa.','Cao 124.9 mm - Rộng 84.3 mm - Sâu 51 mm',11,2483100,0,'Cảm biến Darkfield 8000 DPI theo dõi chính xác trên hầu hết mọi bề mặt, kể cả kính. Nút bấm Quiet Click giảm tiếng ồn đáng kể. Khả năng tùy chỉnh nút phong phú qua Logi Options+ và kết nối đa thiết bị giúp tối ưu hóa quy trình làm việc.',2790000,1,NULL,NULL,NULL,4,'MX Master 3S','141 g',25),(19,0,NULL,NULL,'Apple','Silver','Sau: 4 x Thunderbolt 4, 2 x USB-A, HDMI, 10Gb Ethernet, jack tai nghe 3.5mm. Trước: 2 x Thunderbolt 4, khe cắm thẻ SDXC','2025-06-03 12:37:40.205668','Máy tính để bàn mạnh mẽ với chip M2 Ultra, cổng kết nối đa dạng và thiết kế nhỏ gọn.','Mac Studio với chip M2 Ultra là một cỗ máy trạm nhỏ gọn nhưng sở hữu sức mạnh phi thường. Nó được thiết kế cho các chuyên gia sáng tạo đòi hỏi hiệu năng cực cao cho các tác vụ như chỉnh sửa video 8K, render 3D phức tạp và phát triển phần mềm chuyên sâu.','Cao 95 mm - Rộng 197 mm - Sâu 197 mm',6,98690600,0,'Chip M2 Ultra với CPU 24 nhân và GPU lên đến 76 nhân cung cấp hiệu suất xử lý và đồ họa đáng kinh ngạc. Hệ thống tản nhiệt tiên tiến đảm bảo máy hoạt động ổn định ở cường độ cao, cùng với bộ nhớ RAM và SSD dung lượng lớn, tốc độ cao.',104990000,NULL,'32GB/64GB/128GB','1TB/2TB/4TB',NULL,5,'Mac Studio M2 Ultra','3.6 kg',8),(20,0,'76 Wh','Li-Ion','Asus','Moonlight White','USB-A, USB-C (bao gồm USB 4.0), HDMI 2.1, khe đọc thẻ Micro SD, Jack 3.5mm','2025-06-03 12:37:40.217129','Laptop gaming mỏng nhẹ với màn hình OLED 14-inch và hiệu năng ấn tượng.','ASUS ROG Zephyrus G14 là một laptop gaming siêu di động, nổi bật với sự cân bằng giữa hiệu năng mạnh mẽ và thiết kế nhỏ gọn. Màn hình AniMe Matrix độc đáo (tùy chọn) và chất lượng hiển thị tuyệt vời làm cho nó trở thành lựa chọn hấp dẫn cho game thủ và người sáng tạo nội dung.','Dài 312 mm - Rộng 227 mm - Dày 18.5 mm (không AniMe Matrix)',10,35091000,0,'Với CPU AMD Ryzen series và GPU NVIDIA GeForce RTX series, Zephyrus G14 mang đến hiệu năng ấn tượng trong một thân hình mỏng nhẹ. Máy có khả năng xử lý tốt các tựa game hiện đại và các ứng dụng đồ họa, đồng thời duy trì thời lượng pin tốt cho các tác vụ hàng ngày.',38990000,NULL,'16GB/32GB','1TB SSD','14 inch - Tần số quét 120Hz (OLED)',6,'ROG Zephyrus G14','Khoảng 1.65 kg',27),(21,0,'4000 mAh','Li-Polymer','Keychron','Carbon Black','USB Type-C, Bluetooth 5.1','2025-06-03 12:37:40.230425','Bàn phím cơ không dây cao cấp có thể tùy biến với khung nhôm CNC, đèn RGB và công nghệ QMK/VIA.','Keychron Q1 Pro là một bàn phím cơ custom không dây cao cấp, mang đến trải nghiệm gõ tuyệt vời và khả năng tùy biến sâu rộng. Với khung nhôm CNC chắc chắn, thiết kế gasket mount êm ái và hỗ trợ QMK/VIA, đây là lựa chọn lý tưởng cho những người đam mê bàn phím cơ.','Dài 327.5 mm - Rộng 145 mm - Cao trước 22.6 mm / Cao sau 35.8 mm',10,4491000,0,'Thiết kế gasket mount và plate PC/FR4 mang lại cảm giác gõ mềm mại và âm thanh gõ dễ chịu. Khả năng hot-swap cho phép thay thế switch dễ dàng. Kết nối Bluetooth 5.1 ổn định và pin dung lượng lớn đảm bảo thời gian sử dụng dài lâu.',4990000,1,NULL,NULL,NULL,3,'Q1 Pro','Khoảng 1825 g',28),(22,0,NULL,NULL,'Sony','White','HDMI 2.1, Ethernet, USB-A, USB-C (tin đồn)','2025-06-03 12:37:40.243648','Máy chơi game thế hệ mới với sức mạnh đồ họa vượt trội, SSD siêu nhanh và tay cầm DualSense cải tiến.','Là phiên bản nâng cấp mạnh mẽ của PS5, PS5 Pro được kỳ vọng mang đến trải nghiệm chơi game 4K mượt mà ở tốc độ khung hình cao hơn, hỗ trợ độ phân giải 8K và công nghệ ray tracing tiên tiến hơn nữa. Thiết kế có thể được tinh chỉnh và hệ thống tản nhiệt cải thiện đáng kể để đáp ứng hiệu năng cao hơn.','Cao 388 mm - Rộng 216 mm - Sâu 89 mm (tin đồn)',6,15030600,0,'Với GPU được nâng cấp mạnh mẽ (tin đồn lên đến 60 CUs RDNA 3), CPU Zen2 được tối ưu hóa và RAM nhanh hơn, PS5 Pro hứa hẹn hiệu năng vượt trội, cho phép các nhà phát triển game tạo ra những thế giới chi tiết, sống động hơn với thời gian tải game được rút ngắn và các tính năng đồ họa độc quyền.',15990000,NULL,'16 GB GDDR6 (tin đồn)','2TB SSD (tin đồn)',NULL,4,'Playstation 5 Pro','Khoảng 4.0 - 4.5 kg (ước tính)',30);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_content` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `rating` int NOT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiyof1sindb9qiqr9o8npj8klt` (`product_id`),
  KEY `FKiyf57dy48lyiftdrf7y87rnxi` (`user_id`),
  CONSTRAINT `FKiyf57dy48lyiftdrf7y87rnxi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKiyof1sindb9qiqr9o8npj8klt` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (2,'good','2025-06-04 02:15:28.049445',5,4,8);
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` enum('ADMIN','SELLER','CUSTOMER') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ADMIN'),(2,'SELLER'),(3,'CUSTOMER');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sizes`
--

DROP TABLE IF EXISTS `sizes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sizes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg7yt93nbctnc67y9ae4vohr6c` (`product_id`),
  CONSTRAINT `FKg7yt93nbctnc67y9ae4vohr6c` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sizes`
--

LOCK TABLES `sizes` WRITE;
/*!40000 ALTER TABLE `sizes` DISABLE KEYS */;
INSERT INTO `sizes` VALUES (1,'256GB',25,1),(2,'512GB',15,1),(3,'1TB',10,1),(4,'256GB',29,2),(5,'512GB',30,2),(6,'256GB',19,3),(7,'512GB',15,3),(8,'i5/8GB/512GB SSD/RTX 3050',15,4),(9,'i7/16GB/1TB SSD/RTX 3050Ti',4,4),(10,'Ryzen 9/32GB/2TB SSD/RTX 4090',8,5),(11,'Ryzen 7/16GB/1TB SSD/RTX 4080',0,5),(12,'i5/8GB/512GB SSD',20,6),(13,'i7/16GB/1TB SSD',9,6),(14,'i7/16GB/1TB SSD',9,7),(15,'i5/8GB/512GB SSD',8,7),(16,'25600mAh',50,8),(17,'19200mAh',29,8),(18,'20000mAh',59,9),(19,'10000mAh',40,9),(20,'Một cỡ',28,10),(21,'Một cỡ',45,11),(22,'14.6 inch/128GB',10,12),(23,'14.6 inch/256GB',10,12),(24,'128GB',8,13),(25,'256GB',6,13),(26,'512GB',4,13),(27,'8GB/256GB',20,14),(28,'16GB/512GB',15,14),(29,'16GB/1TB',8,14),(30,'12GB/256GB',20,15),(31,'16GB/512GB',15,15),(32,'i7/16GB/512GB/RTX 4050',12,16),(33,'i7/32GB/1TB/RTX 4060',8,16),(34,'34-inch',18,17),(35,'Standard',29,18),(36,'32GB/1TB',5,19),(37,'64GB/2TB',3,19),(38,'128GB/4TB',2,19),(39,'Ryzen 9/16GB/1TB/RTX 4060',10,20),(40,'Ryzen 9/32GB/1TB/RTX 4070',8,20),(41,'ANSI',20,21),(42,'ISO',14,21),(43,'Digital Edition',15,22),(44,'Disc Edition',25,22);
/*!40000 ALTER TABLE `sizes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `is_banned` bit(1) NOT NULL,
  `business_type` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `oauth_provider` varchar(255) DEFAULT NULL,
  `oauth_provider_id` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `shop_description` varchar(255) DEFAULT NULL,
  `shop_name` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `role_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `UK_t8tbwelrnviudxdaggwr1kd9b` (`email`),
  KEY `FKn82ha3ccdebhokx3a8fgdqeyy` (`role_id`),
  CONSTRAINT `FKn82ha3ccdebhokx3a8fgdqeyy` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,_binary '',_binary '\0',NULL,'2025-06-03 12:22:34.963615','sangshin0987@gmail.com','Sang',NULL,'Nguyen',NULL,NULL,'$2a$10$iMZr3d5gkZzSMbpCET5GrOGrAjZgJvQWFv/BqtvbBTqIlQrT7gFQG',NULL,NULL,NULL,NULL,1),(2,_binary '',_binary '\0',NULL,'2025-06-03 12:22:35.063939','customer1@example.com','Customer',NULL,'One',NULL,NULL,'$2a$10$YFsxtNrTFco1iaYBDQbf6eWLazZtwVaQXJsMsL.NUATXCRgwJHv52','0911111111',NULL,NULL,NULL,3),(3,_binary '',_binary '\0',NULL,'2025-06-03 12:22:35.156950','seller0@example.com','Seller ',NULL,'Two',NULL,NULL,'$2a$10$Az94WX5Eqp4tsOh8IPLH7OJPOOe2UO6IbJZjUEvMKZlUmDOUi7y86','0922222222','Nạp ','Sang ',NULL,2),(4,_binary '',_binary '\0',NULL,'2025-06-03 12:22:35.235213','seller1@example.com','Seller',NULL,'Alpha',NULL,NULL,'$2a$10$hHR9KzhhR4OBZ6P2sVxTNuAmVMy2UmsXtyVum5SNIe1rgAM9Bz6NG','0987654321','Chuyên đồ điện tử gia dụng','Alpha Store',NULL,2),(5,_binary '',_binary '\0',NULL,'2025-06-03 12:22:35.315256','seller2@example.com','Seller',NULL,'Beta',NULL,NULL,'$2a$10$mw.U2hgPovF6IM4MHt.wwer28d8yASa1F1PDqpxsoeNsjtS4zMXOW','0987654322','Nhà sách trực tuyến Beta','Beta Books',NULL,2),(6,_binary '',_binary '\0',NULL,'2025-06-03 12:22:35.394107','seller3@example.com','Seller',NULL,'Gamma',NULL,NULL,'$2a$10$SIoU2jWnlbSbrJqqfXDJ8Olww8TqFgou.27464/efKpqTHSrBGP7a','0987654323','Thời trang Gamma cho mọi nhà','Gamma Fashion',NULL,2),(7,_binary '',_binary '\0',NULL,'2025-06-03 12:22:35.471975','seller4@example.com','Seller',NULL,'Delta',NULL,NULL,'$2a$10$dTDllgun8.fo9xO.RObQGeUYtSGYZPcKITvEZjjaEXil1Dth/vh.6','0987654324','Phụ kiện công nghệ Delta','Delta Gadgets',NULL,2),(8,_binary '',_binary '\0',NULL,'2025-06-03 12:28:40.478519','tansang06092004@gmail.com','sang','https://avatars.githubusercontent.com/u/111638547?v=4','','github','111638547','$2a$10$7F1OrWiuorRKXVQ48.7Sle/PBmDyxPJGwGJCG2jdstIvTBvP1yjle','56456262545',NULL,NULL,NULL,3),(9,_binary '',_binary '\0',NULL,'2025-06-03 14:17:44.326548','customer2@example.com','Customer',NULL,'Two',NULL,NULL,'$2a$10$fcZ7/DxA1xAbsVofpM8fROUmVXZ.y2e/D1i.BKbsORpPSf9viZlh6','0922222222',NULL,NULL,NULL,3),(15,_binary '',_binary '\0',NULL,'2025-06-13 19:09:47.608917','admin@gmail.com','Sang',NULL,'Nguyen',NULL,NULL,'$2a$10$I9T6sDcVyQw0R/.LT.giJuUQd5F23bhRj43Vzrp.ST1qCtnBmBasa',NULL,NULL,NULL,NULL,1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-13 19:24:22
