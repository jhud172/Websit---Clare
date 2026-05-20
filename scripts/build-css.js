const fs = require("fs/promises");
const path = require("path");
const postcss = require("postcss");
const postcssImport = require("postcss-import");
const tailwindcss = require("tailwindcss");
const autoprefixer = require("autoprefixer");

const root = process.cwd();
const sourcePath = path.join(root, "src", "main", "frontend", "styles", "site.css");
const outputPath = path.join(root, "src", "main", "resources", "static", "css", "site.css");

async function build() {
  const source = await fs.readFile(sourcePath, "utf8");
  const imports = source
    .split(/\r?\n/)
    .filter((line) => line.trim().startsWith("@import"))
    .join("\n");

  const tailwindResult = await postcss([tailwindcss, autoprefixer]).process(
    "@tailwind base;\n@tailwind components;\n@tailwind utilities;\n",
    { from: sourcePath },
  );

  const componentResult = await postcss([postcssImport, autoprefixer]).process(imports, {
    from: sourcePath,
  });

  await fs.mkdir(path.dirname(outputPath), { recursive: true });
  await fs.writeFile(outputPath, `${tailwindResult.css}\n\n${componentResult.css}`, "utf8");
}

build().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
