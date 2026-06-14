package com.teatrack_mcd_253eie502802_group02.client;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryProductData {

    public static Map<String, List<Product>> getCategoryProductsMap() {
        Map<String, List<Product>> map = new HashMap<>();

        map.put("Pure Tea", pureTeaProducts());
        map.put("Tea Latte", teaLatteProducts());
        map.put("Milk Tea", milkTeaProducts());
        map.put("New Arrivals", newArrivalsProducts());
        map.put("Best Sellers", bestSellersProducts());
        map.put("Fruit Tea", fruitTeaProducts());

        return map;
    }

    private static List<Product> pureTeaProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("Hồng Trà Ngô Gia", R.mipmap.hongtradailoan, 4.9f, "1k", "13.000", "16.000", "13.000", "16.000"));
        list.add(new Product("Trà Ô Long Mộc Hương", R.mipmap.traolongdao, 4.9f, "1k", "19.000", "22.000", "16.000", "19.000"));
        list.add(new Product("Trà Xanh Bí Đao", R.mipmap.traxanhbidao, 4.9f, "1k", "16.000", "19.000", "16.000", "19.000"));
        return list;
    }

    private static List<Product> teaLatteProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("Sữa Tươi Khoai Môn Nghiền", R.mipmap.suatuoikhoaimon, 4.9f, "1k", "25.000", "29.000", "22.000", "26.000"));
        list.add(new Product("Ô Long Latte", R.mipmap.olonglatte, 4.9f, "1k", "24.000", "28.000", "21.000", "25.000"));
        list.add(new Product("Sữa Tươi Trân Châu Đường Đen", R.mipmap.trasuatranchauduongden, 4.9f, "1k", "26.000", "30.000", "23.000", "27.000"));
        return list;
    }

    private static List<Product> milkTeaProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("Trà Sữa Trân Châu", R.mipmap.trasuatranchauduongden, 4.9f, "1k", "20.000", "24.000", "20.000", "24.000"));
        list.add(new Product("Trà Sữa Khoai Môn", R.mipmap.suatuoikhoaimon, 4.9f, "1k", "22.000", "26.000", "22.000", "26.000"));
        list.add(new Product("Trà Sữa Ô Long", R.mipmap.olonglatte, 4.9f, "1k", "21.000", "25.000", "21.000", "25.000"));
        return list;
    }

    private static List<Product> newArrivalsProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("Trà Bí Đao Ngô Gia", R.mipmap.trabidaongogia, 4.9f, "1k", "16.000", "19.000", "16.000", "19.000"));
        list.add(new Product("Trà Ô Long Đào", R.mipmap.traolongdao, 4.9f, "1k", "18.000", "22.000", "18.000", "22.000"));
        list.add(new Product("Trà Xanh Sữa", R.mipmap.traxanhsua, 4.9f, "1k", "21.000", "25.000", "21.000", "25.000"));
        return list;
    }

    private static List<Product> bestSellersProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("Hồng Trà Đài Loan", R.mipmap.hongtradailoan, 4.9f, "1k", "13.000", "16.000", "13.000", "16.000"));
        list.add(new Product("Trà Sữa Trân Châu Đường Đen", R.mipmap.trasuatranchauduongden, 4.9f, "1k", "20.000", "24.000", "20.000", "24.000"));
        list.add(new Product("Trà Xanh Bí Đao", R.mipmap.traxanhbidao, 4.9f, "1k", "16.000", "19.000", "16.000", "19.000"));
        return list;
    }

    private static List<Product> fruitTeaProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("Trà Xanh Sữa", R.mipmap.traxanhsua, 4.9f, "1k", "21.000", "25.000", "21.000", "25.000"));
        list.add(new Product("Trà Ô Long Đào", R.mipmap.traolongdao, 4.9f, "1k", "18.000", "22.000", "18.000", "22.000"));
        list.add(new Product("Trà Bí Đao Ngô Gia", R.mipmap.trabidaongogia, 4.9f, "1k", "16.000", "19.000", "16.000", "19.000"));
        return list;
    }
}