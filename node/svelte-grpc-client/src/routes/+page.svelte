<script lang="ts">
  import type { PageData } from './$types'

  export let data: PageData
</script>

<h1>Welcome to SvelteKit</h1>
{#if (data)}
  <p>{ data.message }</p>
{:else}
  <p>レスポンスなし</p>
{/if}
