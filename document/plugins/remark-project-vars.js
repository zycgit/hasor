// Replace explicit placeholders without changing fixed versions in release history.
function replaceVariables(value, variables) {
    return value.replace(/@project\.([A-Za-z][A-Za-z0-9_]*)@/g, (token, name) => {
        if (!Object.hasOwn(variables, name) || typeof variables[name] !== 'string') {
            throw new Error(`Unknown or invalid project variable: ${token}`);
        }
        return variables[name];
    });
}

module.exports = function remarkProjectVars(variables) {
    const replace = (value) => replaceVariables(value, variables);
    return function transform(tree) {
        function visit(node) {
            if (['text', 'inlineCode', 'code'].includes(node.type)) {
                node.value = replace(node.value);
            }
            if (['link', 'image', 'definition'].includes(node.type)) {
                node.url = replace(node.url);
                if (node.title) node.title = replace(node.title);
            }
            node.children?.forEach(visit);
        }
        visit(tree);
    };
};

module.exports.replaceVariables = replaceVariables;
