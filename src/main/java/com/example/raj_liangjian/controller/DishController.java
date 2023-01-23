package com.example.raj_liangjian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.raj_liangjian.common.CustomException;
import com.example.raj_liangjian.common.R;
import com.example.raj_liangjian.dto.DishDto;
import com.example.raj_liangjian.entity.*;
import com.example.raj_liangjian.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    SetmealService setmealService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CacheManager cacheManager;

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */

    @PostMapping
    public R<String> AddDish(@RequestBody DishDto dishDto) {
        dishService.AddDish(dishDto);
        /*//清空某个分类下面的菜品缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);*/
        return R.success("添加菜品成功");
    }


    /**
     * 分院条件查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //添加分页构造器
        Page<Dish> p = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        //添加条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        lambdaQueryWrapper.like(name != null && !"".equals(name.trim()), Dish::getName, name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getCreateTime);
        //执行查询
        dishService.page(p, lambdaQueryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(p, dishDtoPage, "records");

        List<Dish> records = p.getRecords();
        List<DishDto> records1 = records.stream().map((record) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record, dishDto);
            Long categoryId = record.getCategoryId();
            //根据id查询分类对象
            Category byId = categoryService.getById(categoryId);
            if (byId != null) {
                dishDto.setCategoryName(byId.getName());
            }


            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(records1);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息
     *
     * @param id
     * @return
     */

    @GetMapping("{id}")
    public R<DishDto> GetId(@PathVariable Long id) {
        //根据id查询
        Dish byId = dishService.getById(id);
        //拷贝到dishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(byId, dishDto);
        //添加条件构造器
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        //条件条件
        lqw.eq(DishFlavor::getDishId, id);
        //填好list集合
        List<DishFlavor> list = dishFlavorService.list(lqw);
        //set进dishDto实现类中
        dishDto.setFlavors(list);

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     *
     * @param dishDto
     * @return
     */
    @Transactional
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        //清空某个分类下面的菜品缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("菜品修成功");
    }

    /**
     * 根据id删除
     *
     * @param ids
     * @return
     */
    @Transactional
    @DeleteMapping
    public R<String> deleteById(Long[] ids) {
        log.info("进入批量删除{}", Arrays.toString(ids));
        //批量处理redis
        batchRedis(ids);
        //如果菜品正在起售无法被删除
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, 1);
        int count = dishService.count(lambdaQueryWrapper);
        if (count > 0) {
            return R.error("菜品正在售卖中,无法被删除!");
        }
        List<Long> list = new ArrayList<>(Arrays.asList(ids));
        dishService.removeByIds(list);
        //条件构造器
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        //添加条件
        lqw.in(DishFlavor::getDishId, ids);
        //执行删除
        dishFlavorService.remove(lqw);
        /*//删除对应的图片
        dishService.removeImgFile(ids);*/


        return R.success("OK");
    }

    /**
     * 停售/启售
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> PutStatus(@PathVariable int status, Long[] ids) {
        log.info("要修改的id:{}修改status修改为{}", ids, status);
        boolean b = dishService.updateStatus(status, ids);
        if (b) {
            //批量redis
            batchRedis(ids);
            return R.success("OK");
        }
        return R.success("NO");
    }


    /**
     * 通过id查询dish
     * v1.0添加redis缓存
     *
     * @param
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> GetDish(HttpSession httpSession, Dish dish) {
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> dishDtos = null;
        //先重redis中获取缓存数据
        dishDtos = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在直接返回
        if (dishDtos != null) {
            return R.success(dishDtos);
        }
        //如果不存在则在数据库里查询添加到菜品数据缓存的redis
        //添加条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        //添加条件
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.eq(dish.getStatus() != null, Dish::getStatus, dish.getStatus());
        //执行查询
        List<Dish> list = dishService.list(lqw);

        dishDtos = list.stream().map((l) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(l, dishDto);
            LambdaQueryWrapper<DishFlavor> lqwDF = new LambdaQueryWrapper<>();
            lqwDF.eq(DishFlavor::getDishId, l.getId());
            List<DishFlavor> list1 = dishFlavorService.list(lqwDF);
            dishDto.setFlavors(list1);
            return dishDto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(key, dishDtos, 60, TimeUnit.MINUTES);

        //返回
        return R.success(dishDtos);
    }

    /**
     * 批量删除数据
     *
     * @param ids
     */
    private void batchRedis(Long[] ids) {
        for (Long id : ids) {
            //清空某个分类下面的菜品缓存
            Dish byId = dishService.getById(id);
            if (byId != null) {
                String key = "dish_" + byId.getCategoryId() + "_1";
                redisTemplate.delete(key);
            } else {
                throw new CustomException("前段数据传输异常");
            }

        }
    }
}
