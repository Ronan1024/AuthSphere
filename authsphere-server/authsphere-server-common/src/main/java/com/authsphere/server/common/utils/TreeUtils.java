package com.authsphere.server.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用树结构构建工具。
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 将扁平节点列表构建为多级树，返回顶级节点。
     *
     * @param nodes          扁平节点列表，输出顺序保持输入顺序
     * @param idGetter       节点 ID 读取器
     * @param parentIdGetter 父节点 ID 读取器
     * @param childrenSetter 子节点写入器
     * @param rootParentId   根节点的父 ID，通常为 null 或 0
     * @param <T>            节点类型
     * @param <K>            节点 ID 类型
     * @return 多级树根节点列表
     */
    public static <T, K> List<T> buildTree(Collection<T> nodes,
                                           Function<T, K> idGetter,
                                           Function<T, K> parentIdGetter,
                                           BiConsumer<T, List<T>> childrenSetter,
                                           K rootParentId) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        Map<K, T> nodeMap = new LinkedHashMap<>();
        Map<K, List<T>> childrenMap = new LinkedHashMap<>();
        for (T node : nodes) {
            K id = idGetter.apply(node);
            nodeMap.put(id, node);
            childrenMap.computeIfAbsent(parentIdGetter.apply(node), ignored -> new ArrayList<>()).add(node);
        }
        List<T> roots = new ArrayList<>();
        for (T node : nodes) {
            childrenSetter.accept(node, childrenMap.getOrDefault(idGetter.apply(node), Collections.emptyList()));

            K parentId = parentIdGetter.apply(node);
            if (Objects.equals(parentId, rootParentId) || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            }
        }
        return roots;
    }
}
