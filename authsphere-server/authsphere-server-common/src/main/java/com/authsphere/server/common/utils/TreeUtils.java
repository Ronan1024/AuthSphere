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
        if (childrenSetter == null) {
            return new ArrayList<>(nodes);
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

    /**
     * 查询指定父节点下的子树。
     *
     * @param nodes          扁平节点列表，输出顺序保持输入顺序
     * @param idGetter       节点 ID 读取器
     * @param parentIdGetter 父节点 ID 读取器
     * @param childrenSetter 子节点写入器
     * @param parentId       要查询的父节点 ID；传 null 表示查询根节点子树
     * @param <T>            节点类型
     * @param <K>            节点 ID 类型
     * @return 指定父节点下的多级子树
     */
    public static <T, K> List<T> findSubTree(Collection<T> nodes,
                                             Function<T, K> idGetter,
                                             Function<T, K> parentIdGetter,
                                             BiConsumer<T, List<T>> childrenSetter,
                                             K parentId) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        Map<K, List<T>> childrenMap = new LinkedHashMap<>();
        for (T node : nodes) {
            childrenMap.computeIfAbsent(parentIdGetter.apply(node), ignored -> new ArrayList<>()).add(node);
        }
        if (childrenSetter == null) {
            return collectDescendants(childrenMap.getOrDefault(parentId, Collections.emptyList()), idGetter, childrenMap);
        }
        return attachChildren(childrenMap.getOrDefault(parentId, Collections.emptyList()), idGetter, childrenSetter, childrenMap);
    }

    private static <T, K> List<T> collectDescendants(Collection<T> nodes,
                                                     Function<T, K> idGetter,
                                                     Map<K, List<T>> childrenMap) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (T node : nodes) {
            result.add(node);
            result.addAll(collectDescendants(childrenMap.get(idGetter.apply(node)), idGetter, childrenMap));
        }
        return result;
    }

    private static <T, K> List<T> attachChildren(Collection<T> nodes,
                                                 Function<T, K> idGetter,
                                                 BiConsumer<T, List<T>> childrenSetter,
                                                 Map<K, List<T>> childrenMap) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (T node : nodes) {
            List<T> children = attachChildren(childrenMap.get(idGetter.apply(node)), idGetter, childrenSetter, childrenMap);
            childrenSetter.accept(node, children);
            result.add(node);
        }
        return result;
    }
}
