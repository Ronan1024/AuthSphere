package com.authsphere.server.common.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 通用树构建工具测试。
 */
class TreeUtilsTest {

    @Test
    void buildTreeShouldKeepOrderAndAttachMultiLevelChildren() {
        TreeNode system = new TreeNode(1L, null, "系统管理");
        TreeNode account = new TreeNode(2L, 1L, "账号管理");
        TreeNode accountCreate = new TreeNode(3L, 2L, "新增账号");
        TreeNode dashboard = new TreeNode(4L, null, "工作台");

        List<TreeNode> tree = TreeUtils.buildTree(
                List.of(system, account, accountCreate, dashboard),
                TreeNode::id,
                TreeNode::parentId,
                TreeNode::children,
                null
        );

        assertEquals(List.of("系统管理", "工作台"), tree.stream().map(TreeNode::name).toList());
        assertEquals(List.of("账号管理"), system.children().stream().map(TreeNode::name).toList());
        assertEquals(List.of("新增账号"), account.children().stream().map(TreeNode::name).toList());
        assertEquals(List.of(), accountCreate.children());
        assertEquals(List.of(), dashboard.children());
    }

    @Test
    void buildTreeShouldKeepOrphanNodesAsRoots() {
        TreeNode orphan = new TreeNode(10L, 999L, "孤儿菜单");
        TreeNode root = new TreeNode(11L, null, "根菜单");

        List<TreeNode> tree = TreeUtils.buildTree(
                List.of(orphan, root),
                TreeNode::id,
                TreeNode::parentId,
                TreeNode::children,
                null
        );

        assertEquals(List.of("孤儿菜单", "根菜单"), tree.stream().map(TreeNode::name).toList());
        assertEquals(List.of(), orphan.children());
    }

    @Test
    void buildTreeShouldReturnFlatListWhenChildrenSetterIsNull() {
        TreeNode system = new TreeNode(1L, null, "系统管理");
        TreeNode account = new TreeNode(2L, 1L, "账号管理");
        TreeNode dashboard = new TreeNode(3L, null, "工作台");

        List<TreeNode> list = TreeUtils.buildTree(
                List.of(system, account, dashboard),
                TreeNode::id,
                TreeNode::parentId,
                null,
                null
        );

        assertEquals(List.of("系统管理", "账号管理", "工作台"), list.stream().map(TreeNode::name).toList());
        assertEquals(null, system.children());
        assertEquals(null, account.children());
        assertEquals(null, dashboard.children());
    }

    @Test
    void findSubTreeShouldReturnChildrenByParentId() {
        TreeNode system = new TreeNode(1L, null, "系统管理");
        TreeNode account = new TreeNode(2L, 1L, "账号管理");
        TreeNode accountCreate = new TreeNode(3L, 2L, "新增账号");
        TreeNode dashboard = new TreeNode(4L, null, "工作台");
        TreeNode orphan = new TreeNode(5L, 999L, "孤儿菜单");

        List<TreeNode> subTree = TreeUtils.findSubTree(
                List.of(system, account, accountCreate, dashboard, orphan),
                TreeNode::id,
                TreeNode::parentId,
                TreeNode::children,
                1L
        );

        assertEquals(List.of("账号管理"), subTree.stream().map(TreeNode::name).toList());
        assertEquals(List.of("新增账号"), subTree.getFirst().children().stream().map(TreeNode::name).toList());
        assertEquals(List.of(), accountCreate.children());
        assertEquals(null, orphan.children());
    }

    @Test
    void findSubTreeShouldReturnRootSubTreeWhenParentIdIsNull() {
        TreeNode system = new TreeNode(1L, null, "系统管理");
        TreeNode account = new TreeNode(2L, 1L, "账号管理");
        TreeNode dashboard = new TreeNode(3L, null, "工作台");

        List<TreeNode> subTree = TreeUtils.findSubTree(
                List.of(system, account, dashboard),
                TreeNode::id,
                TreeNode::parentId,
                TreeNode::children,
                null
        );

        assertEquals(List.of("系统管理", "工作台"), subTree.stream().map(TreeNode::name).toList());
        assertEquals(List.of("账号管理"), system.children().stream().map(TreeNode::name).toList());
    }

    @Test
    void findSubTreeShouldReturnFlatDescendantsWhenChildrenSetterIsNull() {
        TreeNode system = new TreeNode(1L, null, "系统管理");
        TreeNode account = new TreeNode(2L, 1L, "账号管理");
        TreeNode accountCreate = new TreeNode(3L, 2L, "新增账号");
        TreeNode dashboard = new TreeNode(4L, null, "工作台");

        List<TreeNode> list = TreeUtils.findSubTree(
                List.of(system, account, accountCreate, dashboard),
                TreeNode::id,
                TreeNode::parentId,
                null,
                1L
        );

        assertEquals(List.of("账号管理", "新增账号"), list.stream().map(TreeNode::name).toList());
        assertEquals(null, account.children());
        assertEquals(null, accountCreate.children());
    }

    private static class TreeNode {
        private final Long id;
        private final Long parentId;
        private final String name;
        private List<TreeNode> children;

        private TreeNode(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        private Long id() {
            return id;
        }

        private Long parentId() {
            return parentId;
        }

        private String name() {
            return name;
        }

        private List<TreeNode> children() {
            return children;
        }

        private void children(List<TreeNode> children) {
            this.children = children;
        }
    }
}
