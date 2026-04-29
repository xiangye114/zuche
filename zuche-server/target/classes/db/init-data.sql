USE zuche;

INSERT INTO brands (name, logo) VALUES
('Audi', 'https://logo.clearbit.com/audi.com'),
('BMW', 'https://logo.clearbit.com/bmw.com'),
('Benz', 'https://logo.clearbit.com/mercedes-benz.com'),
('Toyota', 'https://logo.clearbit.com/toyota.com'),
('Honda', 'https://logo.clearbit.com/honda.com'),
('VW', 'https://logo.clearbit.com/volkswagen.com'),
('Tesla', 'https://logo.clearbit.com/tesla.com'),
('BYD', 'https://logo.clearbit.com/byd.com');

INSERT INTO categories (name, description) VALUES
('Economy', 'Affordable and practical'),
('Comfort', 'Spacious and comfortable'),
('Luxury', 'High-end experience'),
('SUV', 'Strong off-road capability'),
('Business', 'Professional image'),
('EV', 'Eco-friendly electric');

INSERT INTO stores (name, address, phone, latitude, longitude, status) VALUES
('Beijing Chaoyang Store', 'Beijing Chaoyang District Jianguo Road 88', '010-88888888', 39.9042, 116.4074, 1),
('Shanghai Pudong Store', 'Shanghai Pudong New Area Lujiazui Ring Road 1000', '021-88888888', 31.2304, 121.4737, 1),
('Guangzhou Tianhe Store', 'Guangzhou Tianhe District Zhujiang New Town 10', '020-88888888', 23.1291, 113.2644, 1);

INSERT INTO cars (name, brand_id, category_id, store_id, color, price_per_day, deposit, seats, transmission, fuel_type, image, description, status) VALUES
('Audi A4L', 1, 2, 1, 'Black', 350.00, 5000.00, 5, 'Automatic', 'Gasoline', 'https://images.unsplash.com/photo-1603584173870-7f23fdae1b7a?w=800', 'Audi A4L Luxury Sedan', 1),
('BMW 3 Series', 2, 2, 1, 'White', 380.00, 5500.00, 5, 'Automatic', 'Gasoline', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800', 'BMW 3 Series Sport Sedan', 1),
('Benz C Class', 3, 2, 2, 'Silver', 400.00, 6000.00, 5, 'Automatic', 'Gasoline', 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800', 'Benz C Class Luxury Sedan', 1),
('Toyota Corolla', 4, 1, 2, 'White', 150.00, 2000.00, 5, 'Automatic', 'Gasoline', 'https://images.unsplash.com/photo-1621007947382-bb3c3968e3bb?w=800', 'Toyota Corolla Economy Sedan', 1),
('Tesla Model 3', 7, 6, 3, 'White', 400.00, 6000.00, 5, 'Automatic', 'Electric', 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800', 'Tesla Model 3 Electric Sedan', 1),
('BYD Han EV', 8, 6, 3, 'Black', 350.00, 5000.00, 5, 'Automatic', 'Electric', 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800', 'BYD Han EV Luxury Electric', 1);
