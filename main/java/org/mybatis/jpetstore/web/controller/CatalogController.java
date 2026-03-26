package org.mybatis.jpetstore.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.mybatis.jpetstore.domain.Category;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.domain.Product;
import org.mybatis.jpetstore.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/catalog")
public class CatalogController {

  @Autowired
  private CatalogService catalogService;

  @GetMapping
  public String main() {
    return "catalog/main";
  }

  @GetMapping("/category")
  public String viewCategory(@RequestParam("categoryId") String categoryId, Model model) {
    Category category = catalogService.getCategory(categoryId);
    List<Product> productList = catalogService.getProductListByCategory(categoryId);
    model.addAttribute("category", category);
    model.addAttribute("productList", productList);
    return "catalog/category";
  }

  @GetMapping("/product")
  public String viewProduct(@RequestParam("productId") String productId, Model model) {
    Product product = catalogService.getProduct(productId);
    List<Item> itemList = catalogService.getItemListByProduct(productId);
    model.addAttribute("product", product);
    model.addAttribute("itemList", itemList);
    return "catalog/product";
  }

  @GetMapping("/item")
  public String viewItem(@RequestParam("itemId") String itemId, Model model) {
    Item item = catalogService.getItem(itemId);
    if (item == null) {
      return "redirect:/mvc/catalog";
    }
    model.addAttribute("item", item);
    model.addAttribute("product", item.getProduct());
    model.addAttribute("inStock", catalogService.isItemInStock(itemId));
    return "catalog/item";
  }

  @GetMapping("/search")
  public String searchProducts(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
    List<Product> productList = catalogService.searchProductList(keyword == null ? "" : keyword.toLowerCase());
    model.addAttribute("keyword", keyword);
    model.addAttribute("productList", productList);
    return "catalog/search-products";
  }

  @GetMapping(value = "/category-info", produces = "application/json")
  @ResponseBody
  public Map<String, Object> categoryInfo(@RequestParam("categoryId") String categoryId, HttpServletRequest request) {
    Map<String, Object> result = new HashMap<>();
    Category category = catalogService.getCategory(categoryId);
    if (category == null) {
      result.put("ok", false);
      result.put("message", "category not found");
      return result;
    }
    List<Product> products = catalogService.getProductListByCategory(categoryId);
    result.put("ok", true);
    result.put("categoryId", category.getCategoryId());
    result.put("name", category.getName());
    result.put("description", fixDescriptionImagePaths(category.getDescription(), request));
    result.put("products", products.size() > 6 ? products.subList(0, 6) : products);
    return result;
  }

  @GetMapping(value = "/search-suggestions", produces = "application/json")
  @ResponseBody
  public List<Map<String, String>> searchSuggestions(@RequestParam(name = "keyword", required = false) String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return List.of();
    }
    return catalogService.searchProductList(keyword.toLowerCase()).stream().limit(8).map(product -> {
      Map<String, String> m = new HashMap<>();
      m.put("productId", product.getProductId());
      m.put("name", product.getName());
      return m;
    }).collect(Collectors.toList());
  }

  /**
   * 后端从 DB 拿到的 description 图片 URL 以 "../images/xxx.gif" 形式存储， 当页面路径/上下文路径变化时，浏览器会把 ".." 解析到站点根导致 404。 这里把它改为带
   * contextPath 的绝对路径，确保前端 tooltip 能正确加载图片。
   */
  private String fixDescriptionImagePaths(String description, HttpServletRequest request) {
    if (description == null) {
      return null;
    }
    String contextPath = request == null ? "" : request.getContextPath();
    if (contextPath == null) {
      contextPath = "";
    }
    String replacementBase = contextPath + "/images/";
    return description.replace("../images/", replacementBase);
  }
}
