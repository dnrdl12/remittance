
---

## `docs/api/reference.md`

```markdown
# API Reference

이 페이지는 OpenAPI 스펙(`openapi.json`)을 기반으로 ReDoc를 사용해  
전체 API 스펙을 보여줍니다.

---

아래 영역에 ReDoc가 로드됩니다.

<div id="redoc-container"></div>

<script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
<script>
  // MkDocs 빌드 시 docs/api/openapi.json → /api/openapi.json 경로로 서빙된다고 가정
  Redoc.init('/api/openapi.json', {}, document.getElementById('redoc-container'));
</script>

> `docs/api/openapi.json` 파일에 Springdoc에서 export한 OpenAPI JSON을 복사해 두세요.
